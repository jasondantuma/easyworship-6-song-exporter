/**
 * Created by Jason Dantuma on 2015/10/26.
 */

import java.io.File

import swing._

class MainUi extends MainFrame {

    val txtDatabasePath = new TextField {
        columns = 50
    }
    
    val file = new FileChooser
    
    val exportButton = Button ( UiStrings.en.exportSongs ) {
        processRecords
    }

    title = UiStrings.en.appTitle

    contents = new BoxPanel(Orientation.Vertical){
        contents += new BoxPanel(Orientation.Horizontal){
            contents += new Label( UiStrings.en.pathToDatabase )
            contents += txtDatabasePath
            
            contents += Button ( UiStrings.en.search ){

                file.fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly

                val result = file.showDialog(null,  UiStrings.en.selectFolder )
                if (result == FileChooser.Result.Approve){
                    if (testForDatabaseFiles(file.selectedFile.getPath)){
                        txtDatabasePath.text = file.selectedFile.getPath
                    }
                } else None
            }
            
        }

        contents += Swing.VStrut(10)

        contents += exportButton
        border = Swing.EmptyBorder(10)

    }

    def processRecords: Boolean = {
        true
    }

    def testForDatabaseFiles(path: String): Boolean = {
        val songDbFile = new File(path + "/Songs.db")
        if (!songDbFile.exists()){
            Dialog.showMessage(null, UiStrings.en.ewDatabaseNotFound + songDbFile.getAbsolutePath, "Error", Dialog.Message.Error,null)
            return false
        } else {
            Dialog.showMessage(null, "Database found!", "Success", Dialog.Message.Info,null)
        }

        true
    }
}
