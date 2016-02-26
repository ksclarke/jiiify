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
!function (factory) {
  if (typeof require === 'function' && typeof module !== 'undefined') {
    factory();
  } else if (typeof define === 'function' && define.amd) {
    // AMD loader
    define('jiiify-solr-js/solr_service-proxy', [], factory);
  } else {
    // plain old include
    SolrService = factory();
  }
}(function () {

  /**
 Solr service interface that is used to generate the handler, proxy code, etc.

 @class
  */
  var SolrService = function(eb, address) {

    var j_eb = eb;
    var j_address = address;
    var closed = false;
    var that = this;
    var convCharCollection = function(coll) {
      var ret = [];
      for (var i = 0;i < coll.length;i++) {
        ret.push(String.fromCharCode(coll[i]));
      }
      return ret;
    };

    /**
     Searches Solr using the supplied JSON object for search and the handler for results.

     @public
     @param aJsonObject {Object} A Solr search configured in a JSON object 
     @param aHandler {function} A handler to handle the results of the search 
     */
    this.search = function(aJsonObject, aHandler) {
      var __args = arguments;
      if (__args.length === 2 && (typeof __args[0] === 'object' && __args[0] != null) && typeof __args[1] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"aJsonObject":__args[0]}, {"action":"search"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"aJsonObject":__args[0]}, {"action":"index"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

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
      if (closed) {
        throw new Error('Proxy is closed');
      }
      j_eb.send(j_address, {"aVertx":__args[0]}, {"action":"create"});
      return;
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
      if (closed) {
        throw new Error('Proxy is closed');
      }
      j_eb.send(j_address, {"aVertx":__args[0], "aAddress":__args[1]}, {"action":"createProxy"});
      return;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  if (typeof exports !== 'undefined') {
    if (typeof module !== 'undefined' && module.exports) {
      exports = module.exports = SolrService;
    } else {
      exports.SolrService = SolrService;
    }
  } else {
    return SolrService;
  }
});