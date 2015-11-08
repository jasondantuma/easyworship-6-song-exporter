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

    private var songsFile: File                 = null
    private var wordsFile: File                 = null
    private var songsDb: SqlJetDb               = null
    private var wordsDb: SqlJetDb               = null
    private var exportPath: String              = null
    private var exportRichText: Boolean         = true

    def setup(sourcePath: String): Boolean = {
        songsFile = new File(s"$sourcePath/Songs.db")
        wordsFile = new File(s"$sourcePath/SongWords.db")

        if (!songsFile.exists && !wordsFile.exists){
            return false
        }

        true
    }


    /**
      * Get the number of records in the song database
      * @return songCount: Long - number of songs
      * */
    def songCount: Long = {
        songsDb = SqlJetDb.open(songsFile, true)
        songsDb.beginTransaction(SqlJetTransactionMode.READ_ONLY)
        val count = songsDb.getTable("song").open().getRowCount
        songsDb.commit()
        songsDb.close()
        count
    }

    /**
      * Set the output path for exporting
      * */
    def setExportPath(path:String) = {
        exportPath = path
    }

    def processRecords() = {
        handler.setProgressRecordCount(songCount)

        songsDb = SqlJetDb.open(songsFile, true)
        wordsDb = SqlJetDb.open(wordsFile, true)

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

            // for some reason couldn't select lyrics based on id. using loop until id's match
            do {
                if (wordTable.getInteger("song_id") == songId && !songFound){
                    val words: String = wordTable.getString("words")

                    val songFilename = songTitle.replaceAll("[\\/?!:;@#%&*{}<>$`=]", "") // remove illegal characters from filenames

                    println(songFilename)
                    val outputFile =
                        new File(exportPath + "/" + songFilename + (if (exportRichText) ".rtf" else ".txt"))

                    outputFile.createNewFile()
                    val outWriter = new FileOutputStream(outputFile)
                    try {
                        if (exportRichText)
                            outWriter.write(words.getBytes)
                        else
                            outWriter.write(rtfToPlainText(words).getBytes)
                    } catch {
                        case io: java.io.IOException =>
                            println(s"Could not export $songTitle, File Exception Error")
                    }
                    outWriter.close()
                    count += 1
                    songFound = true
                }
            } while (wordTable.next())

            if (!songFound){
                println(s"WORDS NOT FOUND FOR $songTitle")
            }

            wordTable.first() // go back to first record
        } while (songTable.next())

        wordTable.close()
        songTable.close()

        // finish the transactions
        songsDb.commit()
        wordsDb.commit()

        // close the databases
        songsDb.close()
        wordsDb.close()

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
