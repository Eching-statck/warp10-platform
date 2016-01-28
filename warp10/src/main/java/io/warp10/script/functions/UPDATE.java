//
//   Copyright 2016  Cityzen Data
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//

package io.warp10.script.functions;

import io.warp10.WarpDist;
import io.warp10.continuum.Configuration;
import io.warp10.continuum.gts.GTSHelper;
import io.warp10.continuum.gts.GeoTimeSerie;
import io.warp10.continuum.store.Constants;
import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptStackFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.standalone.Warp;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Updates datapoints from the GTS on the stack.
 * 
 * In the standalone mode, this connects to the Ingress endpoint on localhost.
 * In the distributed mode, this uses a proxy to connect to an HTTP endpoint which will push data into continuum.
 */
public class UPDATE extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  
  private URL url = null;
  
  public UPDATE(String name) {
    super(name);
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    //
    // Extract token
    //
    
    Object otoken = stack.pop();
    
    if (!(otoken instanceof String)) {
      throw new WarpScriptException(getName() + " expects a token on top of the stack.");
    }
    
    String token = (String) otoken;
    
    List<GeoTimeSerie> series = new ArrayList<GeoTimeSerie>();
    
    Object o = stack.pop();
    
    if (o instanceof GeoTimeSerie) {
      if (GTSHelper.nvalues((GeoTimeSerie) o) > 0) {
        series.add((GeoTimeSerie) o);
      }
    } else if (o instanceof List) {
      for (Object oo: (List<Object>) o) {
        if (oo instanceof GeoTimeSerie) {
          if (GTSHelper.nvalues((GeoTimeSerie) oo) > 0) {
            series.add((GeoTimeSerie) oo);
          }
        } else {
          throw new WarpScriptException(getName() + " can only operate on Geo Time Series or a list thereof.");
        }
      }
    } else {
      throw new WarpScriptException(getName() + " can only operate on Geo Time Series or a list thereof.");
    }
    
    //
    // Return immediately if 'series' is empty
    //
    
    if (0 == series.size()) {
      return stack;
    }
    
    //
    // Check that all GTS have a name and were renamed
    //
    
    for (GeoTimeSerie gts: series) {
      if (null == gts.getName() || "".equals(gts.getName())) {
        throw new WarpScriptException(getName() + " can only update Geo Time Series which have a non empty name.");
      }
      if (!gts.isRenamed()) {
        throw new WarpScriptException(getName() + " can only update Geo Time Series which have been renamed.");
      }
    }
    
    //
    // Create the OutputStream
    //
    
    HttpURLConnection conn = null;

    try {

      if (null == url) {
        if (WarpDist.getProperties().containsKey(Configuration.CONFIG_WARPSCRIPT_UPDATE_ENDPOINT)) {
          url = new URL(WarpDist.getProperties().getProperty(Configuration.CONFIG_WARPSCRIPT_UPDATE_ENDPOINT));
        } else {
          throw new WarpScriptException(getName() + " configuration parameter '" + Configuration.CONFIG_WARPSCRIPT_UPDATE_ENDPOINT + "' not set.");
        }
      }

      /*
      if (null == this.proxy) {
        conn = (HttpURLConnection) this.url.openConnection();
      } else {
        conn = (HttpURLConnection) this.url.openConnection(this.proxy);
      }
      */
      conn = (HttpURLConnection) url.openConnection();
      
      conn.setDoOutput(true);
      conn.setDoInput(true);
      conn.setRequestMethod("POST");
      conn.setRequestProperty(Constants.getHeader(Configuration.HTTP_HEADER_UPDATE_TOKENX), token);
      conn.setRequestProperty("Content-Type", "application/gzip");
      conn.setChunkedStreamingMode(16384);
      conn.connect();
      
      OutputStream os = conn.getOutputStream();
      GZIPOutputStream out = new GZIPOutputStream(os);
      PrintWriter pw = new PrintWriter(out);
      
      for (GeoTimeSerie gts: series) {
        gts.dump(pw);
      }
      
      pw.close();
      
      //
      // Update was successful, delete all batchfiles
      //
      
      if (200 != conn.getResponseCode()) {
        throw new WarpScriptException(getName() + " failed to complete successfully.");
      }
      
      //is.close();
      conn.disconnect();          
    } catch (IOException ioe) { 
      throw new WarpScriptException(getName() + " failed.");
    } finally {
      if (null != conn) {
        conn.disconnect();
      }
    }

    return stack;
  }
}