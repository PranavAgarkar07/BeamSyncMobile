package com.example.beamsyncmobile.network

/**
 * Holds the current active BeamSync connection.
 * Set when a QR is scanned, cleared on disconnect.
 */
object CurrentConnection {
    @Volatile
    var connection: ServerConnection? = null
        private set

    fun set(conn: ServerConnection) {
        connection = conn
    }

    fun clear() {
        connection = null
    }
}
