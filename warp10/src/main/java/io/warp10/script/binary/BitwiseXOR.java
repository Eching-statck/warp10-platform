//
//   Copyright 2019-2024  SenX S.A.S.
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

package io.warp10.script.binary;

import java.math.BigInteger;

/**
 * Bitwise XOR of the two operands on top of the stack
 */
public class BitwiseXOR extends BitwiseOperation {

  public BitwiseXOR(String name) {
    super(name);
  }

  @Override
  public long operator(long op1, long op2) {
    return op1 ^ op2;
  }

  @Override
  public BigInteger operator(BigInteger op1, BigInteger op2) {
    return op1.xor(op2);
  }
}
