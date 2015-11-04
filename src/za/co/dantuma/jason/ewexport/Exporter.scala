package za.co.dantuma.jason.ewexport

import java.io.{ByteArrayInputStream, FileOutputStream, File}
import javax.swing.text.rtf.RTFEditorKit

import org.tmatesoft.sqljet.core._
import org.tmatesoft.sqljet.core.table._

import scala.swing.Dialog

/**
 * Created by Jason Dantuma on 2015/10/28.
 *
 * The exporter. Receives a handler to send progress updates to
 *
 * @param handler The ExporterUi handler to send updates to
 */
class Exporter(handler: ExporterUi) extends Thread {

    var songsFile: File                 = null
    var wordsFile: File                 = null
    var songsDb: SqlJetDb               = null
    var wordsDb: SqlJetDb               = null
    var wordsOutputPath: String         = null
    var exportRichText: Boolean         = true

    def setup(sourcePath: String): Boolean = {
        songsFile = new File(sourcePath + "/Songs.db")
        wordsFile = new File(sourcePath + "/SongWords.db")

        if (!songsFile.exists && !wordsFile.exists){
            return false
        }

        songsDb = SqlJetDb.open(songsFile, true)
        wordsDb = SqlJetDb.open(wordsFile, true)
        true
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
            do {
                if (wordTable.getInteger("song_id") == songId && !songFound){
                    val words: String = wordTable.getString("words")

                    val songFilename = songTitle.replaceAll("[\\/?!]", "")

                    println(songFilename)
                    val outputFile =
                        new File(wordsOutputPath + "/" + songFilename + (if (exportRichText) ".rtf" else ".txt"))

                    outputFile.createNewFile()
                    val outWriter = new FileOutputStream(outputFile)
                    if (exportRichText)
                        outWriter.write(words.getBytes)
                    else
                        outWriter.write(rtfToPlainText(words).getBytes)
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

    override def run(): Unit = {
        processRecords()
    }

    def setOutputRichText(richText: Boolean) = {
        exportRichText = richText
    }

    private def rtfToPlainText(string: String): String = {
        val rtfToolkit: RTFEditorKit = new RTFEditorKit
        val doc = rtfToolkit.createDefaultDocument()
        rtfToolkit.read(new ByteArrayInputStream(string.getBytes), doc, 0)
        doc.getText(0, doc.getLength).replaceAll("\\n", "\r\n") // return
    }
}
