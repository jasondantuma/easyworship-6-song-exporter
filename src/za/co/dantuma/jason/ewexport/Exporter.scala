package za.co.dantuma.jason.ewexport

import java.io.File

import org.tmatesoft.sqljet.core._
import org.tmatesoft.sqljet.core.table._

import scala.swing.Dialog

/**
 * Created by Jason Dantuma on 2015/10/28.
 */
class Exporter(handler: ExporterUi) {

    var songsFile: File         = null
    var wordsFile: File         = null
    var songsDb: SqlJetDb       = null
    var wordsDb: SqlJetDb       = null

    def setup(sourcePath: String) ={
        songsFile = new File(sourcePath + "/Songs.db")
        wordsFile = new File(sourcePath + "/SongWords.db")

        songsDb = SqlJetDb.open(songsFile, true)
        wordsDb = SqlJetDb.open(wordsFile, true)
    }

    def songCount: Long = {
        songsDb.beginTransaction(SqlJetTransactionMode.READ_ONLY)
        val count = songsDb.getTable("song").open().getRowCount
        songsDb.commit()
        count
    }

}
