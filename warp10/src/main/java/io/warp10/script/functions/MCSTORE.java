//
//   Copyright 2022  SenX S.A.S.
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

import java.util.Map;

public class MCSTORE extends NamedWarpScriptFunction implements WarpScriptStackFunction {

  public MCSTORE(String name) {
    super(name);
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    Object o = stack.pop();

    if (!(o instanceof Map)) {
      throw new WarpScriptException(getName() + " expects a Map as argument.");
    }

    Map<Object, Object> variables = (Map<Object, Object>) o;

    // Check that each key of the map is either a LONG or a STRING
    for (Object elt: variables.keySet()) {
      if (null != elt && (!(elt instanceof String) && !(elt instanceof Long))) {
        throw new WarpScriptException(getName() + " expects each key to be a STRING or a LONG.");
      }
    }

    for (Map.Entry<Object, Object> entry: variables.entrySet()) {
      Object symbol = entry.getKey();

      if (null == symbol) {
        continue;
      }

      if (symbol instanceof Long) {
        if (null == stack.load(((Long) symbol).intValue())) {
          stack.store(((Long) symbol).intValue(), entry.getValue());
        }
      } else {
        if (!stack.getSymbolTable().containsKey((String) symbol)) {
          stack.store((String) symbol, entry.getValue());
        }
      }
    }

    return stack;
  }
}
