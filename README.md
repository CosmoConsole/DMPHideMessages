_This release is part of the source code release as outlined in [this announcement](https://www.spigotmc.org/threads/deathmessagesprime.48322/page-53#post-3933244)._

# DMPHideMessages for DeathMessagesPrime

Adds a feature that allows individual players to hide/block death messages from specific players. For example, with three players A, B and C, A can hide death messages from C but both B and C will still see them.

Commands:
/dmhide - followed by an UUID or the name of an online player, this will hide death messages from that specific player for you.
/dmshow - followed by an UUID or the name of an online player, this will remove the specific player from your hidden list and death messages from that player will be shown again.
/dmhlist - shows the list of players you have blocked death messages from.

Example with A, B and C:
A: /dmhlist
The output shows (0): A has not yet blocked death messages by anyone.
A: /dmhide C
A will no longer receive death messages coming from C.
A: /dmhlist
The list will now show (1) and list C: A has blocked C's death messages.
A: /dmshow C
A unblocks C, so that A can now see C's death messages again.

Hidden player lists are stored between server restarts.

For permissions, dmphidemessages.hide will allow the usage of the plugin and is granted to all players by default. 
