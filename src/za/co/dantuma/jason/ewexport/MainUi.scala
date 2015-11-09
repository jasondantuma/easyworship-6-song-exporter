package za.co.dantuma.jason.ewexport

/**
 * Created by Jason Dantuma on 2015/10/26.
 *
 * Main UI for the application
 */
import scala.swing._
import scala.swing.Color

class MainUi extends ExporterUi {

    private var Exporter = new Exporter(this)

    val txtDatabasePath = new TextField {
        editable = false
    }

    val txtOutputPath = new TextField {
        editable = false
    }

    val radioRichText = new RadioButton("Rich Text (Maintains Formatting)"){selected = true}

    val radioPlainText = new RadioButton("Plain Text")

    val radioButtons = new BoxPanel(Orientation.Horizontal){
        contents += radioRichText
        contents += radioPlainText
    }

    val outputTypeRadio = new ButtonGroup (radioRichText,radioPlainText)

    val file = new FileChooser

    val exportButton = Button ( UiStrings.en.exportSongs ) {
        processRecords()
    }
    exportButton.enabled = false

    val btnOpenSourceFolder = Button ( UiStrings.en.btnOpenFolder ){

        file.fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly

        val result = file.showDialog( null,  UiStrings.en.selectFolder )
        if (result == FileChooser.Result.Approve) {

            // Exporter .setup does the database file check for us now, and returns false if its not found
            if (Exporter.setup(file.selectedFile.getPath)) {

                txtDatabasePath.text = file.selectedFile.getPath
                exportButton.enabled = true
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
    contents = new GridBagPanel {
        def constraints(x: Int, y: Int,
                        fill: GridBagPanel.Fill.Value = GridBagPanel.Fill.None,
                        gridwidth: Int = 1, gridheight: Int = 1,
                        weightx: Double = 0.0, weighty: Double = 0.0)
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
        add(txtDatabasePath, constraints(2,0,GridBagPanel.Fill.Horizontal,1,1,1,1))
        add(btnOpenSourceFolder, constraints(4,0,GridBagPanel.Fill.Horizontal))

        //Destination
        add(new Label( UiStrings.en.pathToOutput, null, Alignment.Right), constraints(0,1))
        add(txtOutputPath, constraints(2,1, GridBagPanel.Fill.Horizontal,1,1,1,1))
        add(btnOpenDestinationFolder, constraints(4,1,GridBagPanel.Fill.Horizontal))

        add(Swing.VStrut(10), constraints(0,2))
        add(Swing.VStrut(10), constraints(0,4))
        add(Swing.HStrut(10), constraints(1,2))
        add(Swing.HStrut(10), constraints(3,2))

        //export button
        add(radioButtons, constraints(2,3))
        add(exportButton, constraints(4,3))

        border = Swing.EmptyBorder(10)

        add(progressBar, constraints(0,5,GridBagPanel.Fill.Horizontal, 5))

    }

    centerOnScreen() // lets center the window on the screen now
    title = UiStrings.en.appTitle
    minimumSize = size

    def processRecords() = {
        if (txtOutputPath.text.nonEmpty){
            Exporter.setExportPath(txtOutputPath.text)
            Exporter.setOutputRichText(radioRichText.selected)
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
        progressBar.indeterminate = true
        Dialog.showMessage(null, UiStrings.en.exportSuccess)

        // reset the application
        Exporter = new Exporter(this)
        exportButton.enabled = false
        txtDatabasePath.text = ""
        txtOutputPath.text = ""
        progressBar.value = 0
        progressBar.indeterminate = false

    }

    override def exportFailed(reason: String): Unit = {
        Dialog.showMessage(null, reason, "Error", Dialog.Message.Error)
    }
}
