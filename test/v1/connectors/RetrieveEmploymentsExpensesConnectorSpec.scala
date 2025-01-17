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

package v1.connectors

import mocks.MockAppConfig
import uk.gov.hmrc.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.downstream.DownstreamSource
import v1.models.domain.MtdSource
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveEmploymentExpenses.RetrieveEmploymentsExpensesRequest
import v1.models.response.retrieveEmploymentExpenses.{Expenses, RetrieveEmploymentsExpensesResponse}

import scala.concurrent.Future

class RetrieveEmploymentsExpensesConnectorSpec extends ConnectorSpec {

  private val nino = Nino("AA123456A")
  private val taxYear = "2019-20"
  val source: MtdSource.`user`.type = MtdSource.`user`

  class Test extends MockHttpClient with MockAppConfig {
    val connector: RetrieveEmploymentsExpensesConnector = new RetrieveEmploymentsExpensesConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnv returns "des-environment"
  }

  "retrieve employment expenses" should {
    val request = RetrieveEmploymentsExpensesRequest(nino, taxYear, source)

    "return a result" when {
      "the downstream call is successful" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, RetrieveEmploymentsExpensesResponse(
          Some("2019-04-06"),
          Some(2000.99),
          Some(MtdSource.`user`),
          Some("2019-04-06"),
          Some(Expenses(
            Some(2000.99),
            Some(2000.99),
            Some(2000.99),
            Some(2000.99),
            Some(2000.99),
            Some(2000.99),
            Some(2000.99),
            Some(2000.99)
          ))
        )))

        MockedHttpClient
          .get(
            url = s"$baseUrl/income-tax/expenses/employments/$nino/$taxYear?view=${DownstreamSource.`CUSTOMER`}",
            requiredHeaders = requiredHeaders :_*
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveEmploymentExpenses(request)) shouldBe outcome
      }
    }
  }
}
