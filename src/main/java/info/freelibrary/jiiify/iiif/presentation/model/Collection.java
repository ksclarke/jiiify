/*
 * Copyright 2015 Ralf Eichinger
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

package info.freelibrary.jiiify.iiif.presentation.model;

import java.util.List;

import info.freelibrary.jiiify.iiif.presentation.model.other.Metadata;

/**
 * <p>
 * Recommended URI Pattern: {scheme}://{host}/{prefix}/collection/{name}</p>
 *
 * @author Ralf Eichinger
 */
public class Collection extends AbstractIiifResource {

    private String description; // recommended
    private final String label; // required
    private final List<Metadata> metadata; // recommended
    private String thumbnail; // recommended
    private String viewingHint; // optional

    public Collection(final String id, final String label, final List<Metadata> metadata) {
        assert id != null;
        assert label != null;

        this.id = id;
        this.label = label;

        this.metadata = metadata;

        type = "sc:Collection";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public List<Metadata> getMetadata() {
        return metadata;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(final String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getViewingHint() {
        return viewingHint;
    }

    public void setViewingHint(final String viewingHint) {
        this.viewingHint = viewingHint;
    }
}
