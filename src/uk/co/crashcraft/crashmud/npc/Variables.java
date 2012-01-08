package uk.co.crashcraft.crashmud.npc;

public enum Variables {

    NO_PATH, // No pathfinding (Static NPC) [DEFAULT]
    NO_TALK, // Mark the NPC as having no chat/dialog
    NO_PASS, // Mark the NPC as blocking a direction/exit
    REQ_ITM, // Require an Item in order for the user to interact/pass the NPC
    REQ_LVL, // Require a minimum level for the user before interacting/passing the NPC
    BOT_WIZ, // NPC/Bot was made by a Wizard and is part of the key storyline
    BOT_USR, // NPC/Bot was made by a community user and is not official and not be part of the main storyline
    BOT_APP, // NPC/Bot was made by a community user and is not official but has been accepted into the main storyline

}