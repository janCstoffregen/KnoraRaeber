/*
 * Copyright © 2015 Lukas Rosenthaler, Benjamin Geer, Ivan Subotic,
 * Tobias Schweizer, André Kilchenmann, and Sepideh Alassi.
 * This file is part of Knora.
 * Knora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Knora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public
 * License along with Knora.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.knora.webapi.e2e.v1

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import com.typesafe.config.ConfigFactory
import org.knora.webapi.messages.v1.store.triplestoremessages.{RdfDataObject, TriplestoreJsonProtocol}
import org.knora.webapi.{E2ESpec, StartupFlags}
import spray.json._

import scala.concurrent.duration._

object StoreRouteV1E2ESpec {
    val config = ConfigFactory.parseString(
        """
          akka.loglevel = "DEBUG"
          akka.stdout-loglevel = "DEBUG"
        """.stripMargin)
}

/**
  * End-to-End (E2E) test specification for testing the 'v1/store' route.
  *
  * This spec tests the 'v1/store' route.
  */
class StoreRouteV1E2ESpec extends E2ESpec(StoreRouteV1E2ESpec.config) with TriplestoreJsonProtocol {

	/**
      * The marshaling to Json is done automatically by spray, hence the import of the 'TriplestoreJsonProtocol'.
      * The Json which spray generates looks like this:
      *
      *  [
      *     {"path": "_test_data/all_data/incunabula-data.ttl", "name": "http://www.knora.org/data/incunabula"},
      *     {"path": "_test_data/demo_data/images-demo-data.ttl", "name": "http://www.knora.org/data/images"}
      *  ]
      *
      * and could have been supplied to the post request instead of the scala object.
      */
    private val rdfDataObjects: List[RdfDataObject] = List(
        RdfDataObject(path = "_test_data/all_data/incunabula-data.ttl", name = "http://www.knora.org/data/incunabula"),
        RdfDataObject(path = "_test_data/demo_data/images-demo-data.ttl", name = "http://www.knora.org/data/images")
    )

    "The ResetTriplestoreContent Route ('v1/store/ResetTriplestoreContent')" should {

        "succeed with resetting if startup flag is set" in {
            /**
              * This test corresponds to the following curl call:
              * curl -H "Content-Type: application/json" -X POST -d '[{"path":"../knora-ontologies/knora-base.ttl","name":"http://www.knora.org/ontology/knora-base"}]' http://localhost:3333/v1/store/ResetTriplestoreContent
              */

			StartupFlags.allowResetTriplestoreContentOperationOverHTTP send true
			log.debug(s"StartupFlags.allowResetTriplestoreContentOperationOverHTTP = ${StartupFlags.allowResetTriplestoreContentOperationOverHTTP.get}")

            val request = Post(baseApiUrl + "/v1/store/ResetTriplestoreContent", HttpEntity(ContentTypes.`application/json`, rdfDataObjects.toJson.compactPrint))
            val response = singleAwaitingRequest(request, 300.seconds)
            log.debug("==>> " + response.toString)
            assert(response.status === StatusCodes.OK)
        }


        "fail with resetting if startup flag is not set" in {
            StartupFlags.allowResetTriplestoreContentOperationOverHTTP send false
            //log.debug("==>> before")
            val request = Post(baseApiUrl + "/v1/store/ResetTriplestoreContent", HttpEntity(ContentTypes.`application/json`, rdfDataObjects.toJson.compactPrint))
            val response = singleAwaitingRequest(request, 300.seconds)
            //log.debug("==>> " + response.toString)
            assert(response.status === StatusCodes.Forbidden)
        }
    }
}
