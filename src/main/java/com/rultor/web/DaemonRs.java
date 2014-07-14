/**
 * Copyright (c) 2009-2014, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.web;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.jcabi.xml.XML;
import com.rultor.agents.daemons.Tail;
import com.rultor.profiles.Profiles;
import java.io.IOException;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Single daemon.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Path("/t/{number:[0-9]+}-{hash:[a-f0-9]+}")
public final class DaemonRs extends BaseRs {

    /**
     * Talk unique number.
     */
    private transient Long number;

    /**
     * Daemon hash ID.
     */
    private transient String hash;

    /**
     * Inject it from query.
     * @param talk Talk name
     */
    @PathParam("number")
    public void setNumber(@NotNull(message = "talk number is mandatory")
        final Long talk) {
        this.number = talk;
    }

    /**
     * Inject it from query.
     * @param dmn Daemon hash
     */
    @PathParam("hash")
    public void setHash(@NotNull(message = "daemon hash is mandatory")
        final String dmn) {
        this.hash = dmn;
    }

    /**
     * Get front.
     * @return The JAX-RS response
     * @throws IOException If fails
     */
    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public Response index() throws IOException {
        if (!this.talks().exists(this.number)) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                "there is no such page here",
                Level.WARNING
            );
        }
        if (!this.granted()) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                "according to .rultor.yml, you're not allowed to see this",
                Level.WARNING
            );
        }
        return Response.ok().entity(
            new Tail(
                this.talks().get(this.number).read(),
                this.hash
            ).read()
        ).build();
    }

    /**
     * Can I see this talk?
     * @return TRUE if access granted
     * @throws IOException If fails
     */
    private boolean granted() throws IOException {
        final XML xml = new Profiles().fetch(
            this.talks().get(this.number)
        ).read();
        final boolean granted;
        if (xml.nodes("/p/readers").isEmpty()) {
            granted = true;
        } else {
            final String self = this.auth().identity().urn().toString();
            granted = Iterables.any(
                xml.xpath("/p/readers/item/text()"),
                new Predicate<String>() {
                    @Override
                    public boolean apply(final String input) {
                        return input.trim().equals(self);
                    }
                }
            );
        }
        return granted;
    }

}
