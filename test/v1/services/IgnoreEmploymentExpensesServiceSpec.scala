/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.services

import uk.gov.hmrc.domain.Nino
import v1.mocks.connectors.MockIgnoreEmploymentExpensesConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.ignoreEmploymentExpenses.IgnoreEmploymentExpensesRequest

import scala.concurrent.Future

class IgnoreEmploymentExpensesServiceSpec extends ServiceSpec {

  val taxYear = "2021-22"
  val nino = Nino("AA123456A")

  private val requestData = IgnoreEmploymentExpensesRequest(nino, taxYear)

  trait Test extends MockIgnoreEmploymentExpensesConnector {
    val service = new IgnoreEmploymentExpensesService(
      connector = mockIgnoreEmploymentExpensesConnector
    )
  }

  "service" should {
    "return mapped result" when {
      "connector call successful" in new Test {
        MockIgnoreEmploymentExpensesConnector.ignore(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.ignore(requestData)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
  }

  "map errors according to spec" should {
    "connector call unsuccessful" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockIgnoreEmploymentExpensesConnector.ignore(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.ignore(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_TAX_YEAR" -> TaxYearFormatError,
        "INVALID_CORRELATIONID" -> DownstreamError,
        "INVALID_PAYLOAD" -> DownstreamError,
        "INVALID_REQUEST_BEFORE_TAX_YEAR_END" -> RuleTaxYearNotEndedError,
        "SERVER_ERROR" -> DownstreamError,
        "SERVICE_UNAVAILABLE" -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
