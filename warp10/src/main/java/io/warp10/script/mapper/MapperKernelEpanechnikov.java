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

package io.warp10.script.mapper;

/**
 * Epanechnikov kernel
 * 
 * @see http://en.wikipedia.org/wiki/Kernel_%28statistics%29
 */
public class MapperKernelEpanechnikov extends MapperKernel {
  
  public MapperKernelEpanechnikov(String name) {
    super(name);
  }
  
  @Override
  double[] getWeights(long step, int width) {
    double[] weights = new double[1 + (width >>> 1)];
    
    for (int i = 0; i < weights.length; i++) {
      double u = i / (weights.length - 1.0D);
      weights[i] = (3.0D / 4.0D) * (1.0D - (u * u));
    }
    
    return weights;
  }
}