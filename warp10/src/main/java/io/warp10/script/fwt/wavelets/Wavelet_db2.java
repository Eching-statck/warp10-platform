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

package io.warp10.script.fwt.wavelets;

import io.warp10.script.fwt.Wavelet;

public class Wavelet_db2 extends Wavelet {

  private static final int transformWavelength = 2;

  private static final double[] scalingDeComposition = new double[] { -0.12940952255092145, 0.22414386804185735, 0.836516303737469, 0.48296291314469025,  };
  private static final double[] waveletDeComposition = new double[] { -0.48296291314469025, 0.836516303737469, -0.22414386804185735, -0.12940952255092145,  };

  private static final double[] scalingReConstruction = new double[] { 0.48296291314469025, 0.836516303737469, 0.22414386804185735, -0.12940952255092145,  };
  private static final double[] waveletReConstruction = new double[] { -0.12940952255092145, -0.22414386804185735, 0.836516303737469, -0.48296291314469025,  };

  static {
    //
    // Reverse the arrays as we do convolutions
    //
    reverse(scalingDeComposition);
    reverse(waveletDeComposition);
  }

  private static final void reverse(double[] array) {
    int i = 0;
    int j = array.length - 1;
    
    while (i < j) {
      double tmp = array[i];
      array[i] = array[j];
      array[j] = tmp;
      i++;
      j--;
    }
  }

  public int getTransformWavelength() {
    return transformWavelength;
  }

  public int getMotherWavelength() {
    return waveletReConstruction.length;
  }

  public double[] getScalingDeComposition() {
    return scalingDeComposition;
  }

  public double[] getWaveletDeComposition() {
    return waveletDeComposition;
  }

  public double[] getScalingReConstruction() {
    return scalingReConstruction;
  }

  public double[] getWaveletReConstruction() {
    return waveletReConstruction;
  }
}
