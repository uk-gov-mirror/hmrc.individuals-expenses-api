/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.errors._

class AmendOtherExpensesValidatorSpec extends UnitSpec {

  private val validNino = "AA123456A"
  private val validTaxYear = "2018-19"
  private val requestBodyJson = Json.parse(
    """
      |{
      |  "paymentsToTradeUnionsForDeathBenefits": {
      |    "customerReference": "TRADE UNION PAYMENTS",
      |    "expenseAmount": 1223.22
      |  },
      |  "patentRoyaltiesPayments":{
      |    "customerReference": "ROYALTIES PAYMENTS",
      |    "expenseAmount": 1223.22
      |  }
      |}""".stripMargin)

  val validator = new AmendOtherExpensesValidator


  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(AmendOtherExpensesRawData() shouldBe Nil)
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        validator.validate(AmendOtherExpensesRawData("A12344A", validTaxYear, requestBodyJson)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in {
        validator.validate(AmendOtherExpensesRawData(validNino, "20178", requestBodyJson)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an out of range tax year is supplied" in {
        validator.validate(
          AmendOtherExpensesRawData(validNino, "2016-17", requestBodyJson)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        validator.validate(AmendOtherExpensesRawData("A12344A", "20178", requestBodyJson)) shouldBe
          List(NinoFormatError, TaxYearFormatError)
      }
    }
  }
}