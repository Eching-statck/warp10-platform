//
//   Copyright 2018  SenX S.A.S.
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

import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;

import com.geoxp.GeoXPLib;
import com.geoxp.geo.GeoHashHelper;

/**
 * Convert a lat/lon pair to a GeoHash
 */
public class TOGEOHASH extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  
  public TOGEOHASH(String name) {
    super(name);
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    
    Object lon = stack.pop();
    Object lat = stack.pop();
    
    if (!(lon instanceof Number) && !(lat instanceof Number)) {
      throw new WarpScriptException(getName() + " expects a latitude and a longitude on the stack.");
    }
    
    long geoxppoint = GeoXPLib.toGeoXPPoint(((Number) lat).doubleValue(), ((Number) lon).doubleValue());
    String geohash = GeoHashHelper.fromHHCode(geoxppoint, 32);
    
    stack.push(geohash);

    return stack;
  }
}
