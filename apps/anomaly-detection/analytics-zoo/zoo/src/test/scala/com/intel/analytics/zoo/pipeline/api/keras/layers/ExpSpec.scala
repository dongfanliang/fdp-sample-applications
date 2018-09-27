/*
 * Copyright 2018 Analytics Zoo Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.analytics.zoo.pipeline.api.keras.layers

import com.intel.analytics.bigdl.nn.{Exp => BExp}
import com.intel.analytics.zoo.pipeline.api.keras.layers.{Exp => ZExp}
import com.intel.analytics.bigdl.tensor.Tensor
import com.intel.analytics.bigdl.utils.Shape
import com.intel.analytics.zoo.pipeline.api.keras.ZooSpecHelper
import com.intel.analytics.zoo.pipeline.api.keras.serializer.ModuleSerializationTest

import scala.util.Random

class ExpSpec extends ZooSpecHelper {

  "Exp 3D Zoo" should "be the same as BigDL" in {
    val blayer = BExp[Float]()
    val zlayer = ZExp[Float](inputShape = Shape(3, 4))
    zlayer.build(Shape(-1, 3, 4))
    zlayer.getOutputShape().toSingle().toArray should be (Array(-1, 3, 4))
    val input = Tensor[Float](Array(2, 3, 4)).rand()
    compareOutputAndGradInput(blayer, zlayer, input)
  }

  "Exp 4D Zoo" should "be the same as BigDL" in {
    val blayer = BExp[Float]()
    val zlayer = ZExp[Float](inputShape = Shape(2, 3, 4))
    zlayer.build(Shape(-1, 2, 3, 4))
    zlayer.getOutputShape().toSingle().toArray should be (Array(-1, 2, 3, 4))
    val input = Tensor[Float](Array(2, 2, 3, 4)).rand()
    compareOutputAndGradInput(blayer, zlayer, input)
  }

}

class ExpSerialTest extends ModuleSerializationTest {
  override def test(): Unit = {
    val layer = ZExp[Float](inputShape = Shape(2, 3, 4))
    layer.build(Shape(2, 2, 3, 4))
    val input = Tensor[Float](2, 2, 3, 4).apply1(_ => Random.nextFloat())
    runSerializationTest(layer, input)
  }
}
