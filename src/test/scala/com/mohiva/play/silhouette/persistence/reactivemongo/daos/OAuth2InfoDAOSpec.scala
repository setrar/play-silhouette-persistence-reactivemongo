/**
 * Copyright 2015 Mohiva Organisation (license at mohiva dot com)
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
package com.mohiva.play.silhouette.persistence.reactivemongo.daos

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.reactivemongo.exceptions.MongoException
import com.mohiva.play.silhouette.persistence.reactivemongo.{ MongoScope, MongoSpecification, WithMongo }
import play.api.test.{ PlaySpecification, WithApplication }

/**
 * Test case for the [[OAuth2InfoDAO]] class.
 */
class OAuth2InfoDAOSpec extends PlaySpecification with MongoSpecification {

  "The `find` method" should {
    "find an OAuth2 info for the given login info" in new WithMongo with Context {
      val result = await(dao.find(loginInfo))

      result must beSome(oauth2Info)
    }

    "return None if no OAuth2 info for the given login info exists" in new WithMongo with Context {
      val result = await(dao.find(loginInfo.copy(providerKey = "new.key")))

      result should beNone
    }
  }

  "The `add` method" should {
    "add a new OAuth2 info" in new WithMongo with Context {
      await(dao.add(loginInfo.copy(providerKey = "new.key"), oauth2Info)) must be equalTo oauth2Info
      await(dao.find(loginInfo.copy(providerKey = "new.key"))) must beSome(oauth2Info)
    }

    "throws exception if a OAuth2 info already exists" in new WithMongo with Context {
      await(dao.add(loginInfo, oauth2Info)) must throwA[MongoException]
    }
  }

  "The `update` method" should {
    "update an existing OAuth2 info" in new WithMongo with Context {
      val updatedInfo = oauth2Info.copy(tokenType = Some("updated"))

      await(dao.update(loginInfo, updatedInfo)) must be equalTo updatedInfo
      await(dao.find(loginInfo)) must beSome(updatedInfo)
    }

    "throw exception if no OAuth2 info for the given login info exists" in new WithMongo with Context {
      await(dao.update(loginInfo.copy(providerKey = "new.key"), oauth2Info)) must throwA[MongoException]
    }
  }

  "The `save` method" should {
    "insert a new OAuth2 info" in new WithMongo with Context {
      await(dao.save(loginInfo.copy(providerKey = "new.key"), oauth2Info)) must be equalTo oauth2Info
      await(dao.find(loginInfo.copy(providerKey = "new.key"))) must beSome(oauth2Info)
    }

    "update an existing OAuth2 info" in new WithMongo with Context {
      val updatedInfo = oauth2Info.copy(tokenType = Some("updated"))

      await(dao.update(loginInfo, updatedInfo)) must be equalTo updatedInfo
      await(dao.find(loginInfo)) must beSome(updatedInfo)
    }
  }

  "The `remove` method" should {
    "remove an OAuth2 info" in new WithMongo with Context {
      await(dao.remove(loginInfo))
      await(dao.find(loginInfo)) must beNone
    }
  }

  /**
   * The context.
   */
  trait Context extends MongoScope {
    self: WithApplication =>

    /**
     * The test fixtures to insert.
     */
    override val fixtures = Map(
      "auth.info.oauth2" -> Seq("oauth2.info.json")
    )

    /**
     * The OAuth2 info DAO implementation.
     */
    lazy val dao = app.injector.instanceOf[OAuth2InfoDAO]

    /**
     * A login info.
     */
    lazy val loginInfo = LoginInfo("facebook", "134405962728980")

    /**
     * A OAuth2 info.
     */
    lazy val oauth2Info = OAuth2Info(
      accessToken = "my.access.token",
      tokenType = Some("bearer"),
      expiresIn = Some(5183836),
      refreshToken = Some("my.refresh.token"),
      params = Some(Map(
        "param1" -> "value1",
        "param2" -> "value2"
      ))
    )
  }
}
