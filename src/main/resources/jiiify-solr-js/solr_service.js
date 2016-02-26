/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

/** @module jiiify-solr-js/solr_service */
var utils = require('vertx-js/util/utils');
var Vertx = require('vertx-js/vertx');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JSolrService = info.freelibrary.jiiify.services.SolrService;

/**
 Solr service interface that is used to generate the handler, proxy code, etc.

 @class
*/
var SolrService = function(j_val) {

  var j_solrService = j_val;
  var that = this;

  /**
   Searches Solr using the supplied JSON object for search and the handler for results.

   @public
   @param aJsonObject {Object} A Solr search configured in a JSON object 
   @param aHandler {function} A handler to handle the results of the search 
   */
  this.search = function(aJsonObject, aHandler) {
    var __args = arguments;
    if (__args.length === 2 && (typeof __args[0] === 'object' && __args[0] != null) && typeof __args[1] === 'function') {
      j_solrService["search(io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](utils.convParamJsonObject(aJsonObject), function(ar) {
      if (ar.succeeded()) {
        aHandler(utils.convReturnJson(ar.result()), null);
      } else {
        aHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Indexes content from the supplied JSON object in Solr.

   @public
   @param aJsonObject {Object} The information to be indexed in Solr 
   @param aHandler {function} A handler to handle the result of the indexing 
   */
  this.index = function(aJsonObject, aHandler) {
    var __args = arguments;
    if (__args.length === 2 && (typeof __args[0] === 'object' && __args[0] != null) && typeof __args[1] === 'function') {
      j_solrService["index(io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](utils.convParamJsonObject(aJsonObject), function(ar) {
      if (ar.succeeded()) {
        aHandler(ar.result(), null);
      } else {
        aHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_solrService;
};

/**
 Creates a service object from the  implementation.

 @memberof module:jiiify-solr-js/solr_service
 @param aVertx {Vertx} A reference to the Vertx object 
 @return {SolrService} A new Solr service object
 */
SolrService.create = function(aVertx) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(JSolrService["create(io.vertx.core.Vertx)"](aVertx._jdel), SolrService);
  } else throw new TypeError('function invoked with invalid arguments');
};

/**
 Creates a proxy object for the Solr service.

 @memberof module:jiiify-solr-js/solr_service
 @param aVertx {Vertx} A reference to the Vertx object 
 @param aAddress {string} A string address at which the proxy will respond 
 @return {SolrService} A Solr service proxy
 */
SolrService.createProxy = function(aVertx, aAddress) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string') {
    return utils.convReturnVertxGen(JSolrService["createProxy(io.vertx.core.Vertx,java.lang.String)"](aVertx._jdel, aAddress), SolrService);
  } else throw new TypeError('function invoked with invalid arguments');
};

// We export the Constructor function
module.exports = SolrService;