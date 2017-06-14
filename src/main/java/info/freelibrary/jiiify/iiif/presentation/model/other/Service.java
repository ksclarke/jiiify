/*
 * Copyright 2015 Ralf Eichinger.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.freelibrary.jiiify.iiif.presentation.model.other;

/**
 * <p>
 * A link to a service that makes more functionality available for the resource, such as from an image to the base URI
 * of an associated IIIF Image API service. The service resource should have additional information associated with it
 * in order to allow the client to determine how to make appropriate use of it, such as a profile link to a service
 * description. It may also have relevant information copied from the service itself. This duplication is permitted in
 * order to increase the performance of rendering the object without necessitating additional HTTP requests.
 * </p>
 * <p>
 * Please see the <a href="http://iiif.io/api/annex/services/">Service Profiles document</a> for known services.
 * </p>
 * <p>
 * Services may be included either by reference or embedded within the response. The decision as to whether to embed
 * or reference is left up to the implementer, however embedded descriptions should be kept as short as possible. If
 * the only properties of the object are @context, @id, profile and/or label, then the client should retrieve the
 * resource from the URI given in @id.
 * </p>
 * Example: <pre>
 * {
 *   "service": {
 *     "@context": "http://example.org/ns/jsonld/context.json",
 *     "@id": "http://example.org/service/example.json",
 *     "profile": "http://example.org/docs/example-service.html",
 *     "label": "Example Service"
 *     // Additional keys may be embedded here, if not then the @id should be retrieved
 *   }
 * }
 * </pre>
 *
 * @author Ralf Eichinger
 */
@Deprecated
public class Service {

    protected String myContext;

    protected String myID;

    protected String myLabel;

    protected String myProfile;

    /**
     * Creates a new service object.
     */
    public Service() {
    }

    /**
     * Creates a new service object with an ID.
     *
     * @param aID An ID for the service object
     */
    public Service(final String aID) {
        myID = aID;
    }

    /**
     * Gets the service's label.
     *
     * @return The service label
     */
    public String getLabel() {
        return myLabel;
    }

    /**
     * Sets the service's label.
     *
     * @param aLabel Services may have a label property to provide a human readable string to display to the user in
     *        the situation that the service has to be selected or manually linked to rather than automatically
     *        processed.
     */
    public void setLabel(final String aLabel) {
        myLabel = aLabel;
    }

    /**
     * Gets the service's context.
     *
     * @return The service's context
     */
    public String getContext() {
        return myContext;
    }

    /**
     * Sets the service's context.
     *
     * @param aContext A service context
     */
    public void setContext(final String aContext) {
        myContext = aContext;
    }

    /**
     * Gets the service ID.
     *
     * @return The service ID
     */
    public String getId() {
        return myID;
    }

    /**
     * Sets the service ID.
     *
     * @param aID Services should have an @id that can be dereferenced, and if so, the representation retrieved from
     *        that URI should be JSON-LD. The service at the URI in @id may require additional parameters, generate
     *        representations other than JSON-LD, or have no JSON-LD representation at all. If a IIIF Image API
     *        service is available for the image, then a link to the service’s base URI should be included.
     */
    public void setId(final String aID) {
        myID = aID;
    }

    /**
     * Gets the service profile.
     *
     * @return The service profile
     */
    public String getProfile() {
        return myProfile;
    }

    /**
     * Sets the service profile.
     *
     * @param aProfile Services should have a profile URI which can be used to determine the type of service,
     *        especially for services that do not provide a JSON-LD representation. The representation retrieved from
     *        the profile URI should be a human or machine readable description of the service.
     */
    public void setProfile(final String aProfile) {
        myProfile = aProfile;
    }

}
