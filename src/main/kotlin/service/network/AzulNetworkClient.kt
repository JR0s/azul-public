package service.network

import edu.udo.cs.sopra.ntf.*
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.response.CreateGameResponse
import tools.aqua.bgw.net.common.response.CreateGameResponseStatus
import tools.aqua.bgw.net.common.response.GameActionResponse
import tools.aqua.bgw.net.common.response.GameActionResponseStatus
import tools.aqua.bgw.net.common.response.JoinGameResponse
import tools.aqua.bgw.net.common.response.JoinGameResponseStatus
import java.io.PrintStream

/**
 * Client für Netzwerk-Kommunikation
 *
 * @param playerName Spielername der den Client benutzt
 * @param host
 * @param secret Netzwerk secret
 * @property networkService
 */
class AzulNetworkClient(
    playerName: String,
    host: String,
    secret: String,
    var networkService: NetworkService,
): BoardGameClient(playerName, host, secret, NetworkLogging.VERBOSE){

    var sessionID: String? = null

    var players: MutableList<String> = mutableListOf()

    /**
     * Umgang mit einer [CreateGameResponse] die vom Server kommt.
     * Host wartet das andere Spieler joinen.
     */
    override fun onCreateGameResponse(response: CreateGameResponse){
        BoardGameApplication.runOnGUIThread {
            check(networkService.connectionState == ConnectionState.WAITING_FOR_HOST) {"Wrong connectionState"}

            when(response.status){
                CreateGameResponseStatus.SUCCESS -> {
                    networkService.updateConnectionState(ConnectionState.WAITING_FOR_GUEST)
                    sessionID = response.sessionID
                    players.add(playerName)
                }
                else -> {
                    networkService.disconnect()
                    error(response)
                }
            }
        }
    }

    /**
     * Umgang mit einer [JoinGameResponse] die vom Server kommt.
     * Gäste warten auf die Init Message des Hosts.
     */
    override fun onJoinGameResponse(response: JoinGameResponse) {
        BoardGameApplication.runOnGUIThread{
            check(networkService.connectionState == ConnectionState.WAITING_FOR_JOIN) {"Wrong connectionState"}

            when(response.status){
                JoinGameResponseStatus.SUCCESS -> {
                    networkService.updateConnectionState(ConnectionState.WAITING_FOR_INIT)
                    sessionID = response.sessionID
                }
                else -> {
                    networkService.disconnect()
                    error(response)
                }
            }
        }
    }

    /**
     * Umgang mit einer [PlayerJoinedNotification] die vom Server kommt.
     * Host fügt die Spieler in Liste hinzu und falls 4 Spieler da sind wird das Spiel automatisch gestartet.
     */
    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        BoardGameApplication.runOnGUIThread{
            check( networkService.connectionState == ConnectionState.WAITING_FOR_GUEST
                    || networkService.connectionState == ConnectionState.WAITING_FOR_INIT) {"Wrong connectionState"}
            //players noch an network service weiterleiten für GUI yk
            players.add(notification.sender)
            networkService.addNetworkPlayer(players)
            //Autostart wenn 4 Spieler insgesamt
            if(players.size==4){
                networkService.startNewHostedGame(players)
            }
        }
    }

    /**
     * Umgang mit einer [GameActionResponse] die vom Server kommt.
     * Wenn es Problem gibt, wird disconnected.
     */
    override fun onGameActionResponse(response: GameActionResponse) {
        BoardGameApplication.runOnGUIThread{
            check(
                networkService.connectionState == ConnectionState.PLAYING_MY_TURN
                        || networkService.connectionState == ConnectionState.WAITING_FOR_OPPONENT
                        || networkService.connectionState == ConnectionState.SELECTED_TILES
                        || networkService.connectionState == ConnectionState.END_ROUND
            )
            { "Wrong connectionState" }
            when (response.status) {
                GameActionResponseStatus.SUCCESS -> {}
                else -> {
                    networkService.disconnect()
                    error(response)
                }
            }
        }
    }

    /**
     * Umgang mit einer [InitMessage] die vom Server kommt.
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onInitMessage(message : InitMessage, sender: String){
        BoardGameApplication.runOnGUIThread{
            check(networkService.connectionState == ConnectionState.WAITING_FOR_INIT) { "Wrong connectionState" }
            networkService.startNewJoinedGame(message,playerName)
        }
    }

    /**
     * handle a [SelectTileMessage] sent by the server
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onSelectTilesMessage(message : SelectTileMessage,sender: String){
        BoardGameApplication.runOnGUIThread{
            check(
                networkService.connectionState == ConnectionState.WAITING_FOR_OPPONENT)
            { "Wrong connectionState" }

            networkService.receiveSelectTilesMessage(message)
        }
    }

    /**
     * handle a [PlaceTilesMessage] sent by the server
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onPlaceTilesMessage(message : PlaceTilesMessage, sender : String){
        BoardGameApplication.runOnGUIThread{
            check(
                networkService.connectionState == ConnectionState.WAITING_FOR_OPPONENT)
            { "Wrong connectionState" }

            networkService.receivePlaceTilesMessage(message, sender, playerName)
        }
    }

    /**
     * Umgang mit einer [RefillBagMessage] die vom Server kommt.
     */
    @Suppress( "UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onRefillBagMessage(message : RefillBagMessage, sender: String){
        BoardGameApplication.runOnGUIThread{
            //check(networkService.connectionState == ConnectionState.END_ROUND) { "not end of round" }
            networkService.receiveRefillBagMessage(message,playerName)
        }
    }
}