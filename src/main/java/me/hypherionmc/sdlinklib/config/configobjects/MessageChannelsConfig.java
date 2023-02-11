/*
 * This file is part of sdlink-lib, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 - 2023 HypherionSA and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.hypherionmc.sdlinklib.config.configobjects;

import me.hypherionmc.moonconfig.core.conversion.Path;
import me.hypherionmc.moonconfig.core.conversion.SpecComment;
import me.hypherionmc.sdlinklib.discord.messages.MessageDestination;

/**
 * @author HypherionSA
 * @date 19/06/2022
 */
public class MessageChannelsConfig {

    @Path("chat")
    @SpecComment("Control where CHAT messages are delivered")
    public DestinationObject chat = DestinationObject.of(MessageDestination.CHAT, false);

    @Path("startStop")
    @SpecComment("Control where START/STOP messages are delivered")
    public DestinationObject startStop = DestinationObject.of(MessageDestination.EVENT, false);

    @Path("joinLeave")
    @SpecComment("Control where JOIN/LEAVE messages are delivered")
    public DestinationObject joinLeave = DestinationObject.of(MessageDestination.EVENT, false);

    @Path("advancements")
    @SpecComment("Control where ADVANCEMENT messages are delivered")
    public DestinationObject advancements = DestinationObject.of(MessageDestination.EVENT, false);

    @Path("death")
    @SpecComment("Control where DEATH messages are delivered")
    public DestinationObject death = DestinationObject.of(MessageDestination.EVENT, false);

    @Path("commands")
    @SpecComment("Control where COMMAND messages are delivered")
    public DestinationObject commands = DestinationObject.of(MessageDestination.EVENT, false);


    public static class DestinationObject {
        @Path("channel")
        @SpecComment("The Channel the message will be delivered to. Valid entries are CHAT, EVENT, CONSOLE")
        public MessageDestination channel;

        @Path("useEmbed")
        @SpecComment("Should the message be sent using EMBED style messages")
        public boolean useEmbed;

        DestinationObject(MessageDestination destination, boolean useEmbed) {
            this.channel = destination;
            this.useEmbed = useEmbed;
        }

        public static DestinationObject of(MessageDestination destination, boolean useEmbed) {
            return new DestinationObject(destination, useEmbed);
        }
    }
}
