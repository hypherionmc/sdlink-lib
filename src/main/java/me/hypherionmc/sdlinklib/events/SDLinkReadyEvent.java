package me.hypherionmc.sdlinklib.events;

import com.hypherionmc.craterlib.core.event.CraterEvent;

public class SDLinkReadyEvent extends CraterEvent {

    public SDLinkReadyEvent() {}

    @Override
    public boolean canCancel() {
        return false;
    }
}