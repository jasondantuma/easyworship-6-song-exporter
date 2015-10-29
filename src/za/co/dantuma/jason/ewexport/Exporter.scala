package za.co.dantuma.jason.ewexport

import java.io.{FileOutputStream, File}

import org.tmatesoft.sqljet.core._
import org.tmatesoft.sqljet.core.table._

import scala.swing.Dialog

/**
 * Created by Jason Dantuma on 2015/10/28.
 */
class Exporter(handler: ExporterUi) {

    var songsFile: File                 = null
    var wordsFile: File                 = null
    var songsDb: SqlJetDb               = null
    var wordsDb: SqlJetDb               = null
    var wordsOutputPath: String         = null

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

    def setWordOutputPath(path:String) = {
        wordsOutputPath = path
    }

    def processRecords() = {
        handler.setProgressRecordCount(songCount)

        songsDb.beginTransaction(SqlJetTransactionMode.READ_ONLY)
        wordsDb.beginTransaction(SqlJetTransactionMode.READ_ONLY)


        val songTable = songsDb.getTable("song").open()
        val wordTable = wordsDb.getTable("word").open()

        var count: Int = 0
        do {

            handler.updateProgress(count)
            val songId = songTable.getInteger("rowid")
            val songTitle = songTable.getString("title")
            var songFound = false
            do{
                if (wordTable.getInteger("song_id") == songId && !songFound){
                    val words: String = wordTable.getString("words")

                    val songFilename = songTitle.replaceAll("[\\/?!]", "")

                    println(songFilename)
                    val outputFile = new File(wordsOutputPath + "/" + songFilename + ".rtf")
                    println(outputFile.getAbsolutePath)

                    outputFile.createNewFile()
                    val outWriter = new FileOutputStream(outputFile)
                    outWriter.write(words.getBytes)
                    outWriter.close()
                    count += 1
                    songFound = true
                }
            } while (wordTable.next())

            if (!songFound){
                println("!!!!!!!!!!!!!!!!SONG NOT FOUND!!!!!!!!!!!!!!!")
            }

            wordTable.first()
        } while (songTable.next())

        wordTable.close()
        songTable.close()

        songsDb.commit()
        wordsDb.commit()

        handler.exportComplete()
    }

}
