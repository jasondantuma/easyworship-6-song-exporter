package za.co.dantuma.jason.ewexport

/**
 * Created by Jason Dantuma on 2015/10/26.
 */

import java.io.File

import scala.swing._

class MainUi extends ExporterUi {

    val Exporter = new Exporter(this)

    val txtDatabasePath = new TextField {
        columns = 50
        editable = false
    }

    val txtOutputPath = new TextField {
        columns = 50
        editable = false
    }

    val file = new FileChooser

    val exportButton = Button ( UiStrings.en.exportSongs ) {
        processRecords()
    }
    exportButton.enabled = false

    val btnOpenSourceFolder = Button ( UiStrings.en.btnOpenFolder ){

        file.fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly

        val result = file.showDialog( null,  UiStrings.en.selectFolder )
        if (result == FileChooser.Result.Approve) {
            if (testForDatabaseFiles(file.selectedFile.getPath)) {
                txtDatabasePath.text = file.selectedFile.getPath
                exportButton.enabled = true

                Exporter.setup(file.selectedFile.getPath)

                Dialog.showMessage(null,
                    UiStrings.en.ewDatabaseFoundPrefix + Exporter.songCount + UiStrings.en.ewDatabaseFoundSuffex)
            }
            else {
                txtDatabasePath.text = null
                exportButton.enabled = false
                Dialog.showMessage(null, UiStrings.en.ewDatabaseNotFoundPrefix + file.selectedFile.getPath)
            }
        }
    }

    val btnOpenDestinationFolder = Button ( UiStrings.en.btnOpenFolder ){

        file.fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly

        val result = file.showDialog( null,  UiStrings.en.selectFolder )
        if (result == FileChooser.Result.Approve){
            txtOutputPath.text = file.selectedFile.getPath
        } else None
    }

    val progressBar = new ProgressBar {
        max = 100
        min = 0
        value = 0
    }



    // ======= END GLOBAL VARS

    title = UiStrings.en.appTitle

    contents = new GridBagPanel {
        def constraints(x: Int, y: Int,
                        gridwidth: Int = 1, gridheight: Int = 1,
                        weightx: Double = 0.0, weighty: Double = 0.0,
                        fill: GridBagPanel.Fill.Value = GridBagPanel.Fill.Horizontal)
        : Constraints = {
            val c = new Constraints
            c.gridx = x
            c.gridy = y
            c.gridwidth = gridwidth
            c.gridheight = gridheight
            c.weightx = weightx
            c.weighty = weighty
            c.fill = fill
            c
        }

        // Source
        add(new Label( UiStrings.en.pathToDatabase, null, Alignment.Right), constraints(0,0))
        add(txtDatabasePath, constraints(2,0))
        add(btnOpenSourceFolder, constraints(4,0))

        //Destination
        add(new Label( UiStrings.en.pathToOutput, null, Alignment.Right), constraints(0,1))
        add(txtOutputPath, constraints(2,1))
        add(btnOpenDestinationFolder, constraints(4,1))

        add(Swing.VStrut(10), constraints(0,2))
        add(Swing.VStrut(10), constraints(0,4))
        add(Swing.HStrut(10), constraints(1,2))
        add(Swing.HStrut(10), constraints(3,2))

        //export button
        add(exportButton, constraints(4,3))

        border = Swing.EmptyBorder(10)

        add(progressBar, constraints(0,5,5))

    }

    centerOnScreen() // lets center the window on the screen now

    def testForDatabaseFiles(path: String): Boolean = {
        val songDbFile = new File(path + "/Songs.db")

        if (!songDbFile.exists()){
            Dialog.showMessage(
                null, UiStrings.en.ewDatabaseNotFoundPrefix + songDbFile.getParent, null, Dialog.Message.Error,null)
            return false
        }

        true
    }

    def processRecords() = {
        if (txtOutputPath.text.length != 0){
            Exporter.setWordOutputPath(txtOutputPath.text)
            Exporter.start()
        } else {
            Dialog.showMessage(null, UiStrings.en.pathToOutputBlank, null, Dialog.Message.Error)
        }
    }

    def setProgressRecordCount(count: Long) = {
        progressBar.max = count.toInt
    }

    def updateProgress(value: Int) = {
        progressBar.value = value
    }

    override def exportComplete() {
        Dialog.showMessage(null, UiStrings.en.exportSuccess)
    }
}
