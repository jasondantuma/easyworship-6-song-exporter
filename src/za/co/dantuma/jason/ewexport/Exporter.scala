package za.co.dantuma.jason.ewexport

import java.io.{ByteArrayInputStream, FileOutputStream, File}
import javax.swing.text.rtf.RTFEditorKit

import org.tmatesoft.sqljet.core._
import org.tmatesoft.sqljet.core.table._

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

    /**
      * Setup the exporter
      * @param sourcePath Path to EasyWorship 6 database files
      * */
    def setup(sourcePath: String): Boolean = {
        songsFile = new File(s"$sourcePath/Songs.db")
        wordsFile = new File(s"$sourcePath/SongWords.db")

        if (!songsFile.exists && !wordsFile.exists){
            return false
        }

        true
    }

    override def run(): Unit = {
        processRecords()
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
      * @param path The path to export to.
      * */
    def setExportPath(path:String) = {
        exportPath = path
    }

    private def processRecords() = {
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
            var songTitle: String = null

            do {
                if (songTable.getInteger("rowid") == wordTable.getInteger("song_id"))
                    songTitle = songTable.getString("title")
            } while (songTable.next() && songTitle == null) // only move on if song is not found

            songTable.first()

            val words: String = wordTable.getString("words")
            println(s"song title: $songTitle")

            if (songTitle != null)
                writeFile(songTitle, words, exportRichText)

            count += 1
        } while (wordTable.next())

        // finish the transactions
        songsDb.commit()
        wordsDb.commit()

        // close the databases
        songsDb.close()
        wordsDb.close()

        handler.exportComplete()
    }

    /**
      * Set to export in rich text (.rtf) or plain text (.txt)
      * */
    def setOutputRichText(richText: Boolean) = {
        exportRichText = richText
    }

    private def rtfToPlainText(string: String): String = {
        val rtfToolkit: RTFEditorKit = new RTFEditorKit
        val doc = rtfToolkit.createDefaultDocument()
        rtfToolkit.read(new ByteArrayInputStream(string.getBytes), doc, 0)
        doc.getText(0, doc.getLength).replaceAll("\\n", "\r\n") // return
    }

    private def cleanFilename(string: String): String = {
        // remove illegal characters from filenames
        string.replaceAll("[\\/?!:;@#%&*{}<>$`=]", "")
    }

    private def writeFile(filename: String, contents: String, richText: Boolean) = {
        var outputFile: File = null
        var nameCollisionCounter = 0

        // a simple loop that makes sure we don't overwrite an existing file. runs once if no name collisions happen
        while (outputFile == null || outputFile.exists()){
            outputFile = new File(
                s"$exportPath/" + // output path
                    cleanFilename(filename) + // append filename
                    (if (nameCollisionCounter > 0) s" ($nameCollisionCounter)" else "") + // append file increment
                    (if (richText) ".rtf" else ".txt") // append file extention
            )
            nameCollisionCounter += 1
        }

        outputFile.createNewFile()
        val outWriter = new FileOutputStream(outputFile)

        try {
            if (richText)
                outWriter.write(contents.getBytes)
            else
                outWriter.write(rtfToPlainText(contents).getBytes)
        } catch {
            case io: java.io.IOException =>
                println(s"Could not write $filename, " + io.getMessage)
                println(io.getStackTrace)
        }

        outWriter.close()
    }
}
