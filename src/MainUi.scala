/**
 * Created by Jason Dantuma on 2015/10/26.
 */

import java.io.File

import swing._

class MainUi extends MainFrame {

    val txtDatabasePath = new TextField {
        columns = 50
    }

    val txtOutputPath = new TextField {
        columns = 50
    }
    
    val file = new FileChooser
    
    val exportButton = Button ( UiStrings.en.exportSongs ) {
        processRecords
    }

    title = UiStrings.en.appTitle

    contents = new BoxPanel(Orientation.Vertical){

        // Source path
        contents += new BoxPanel(Orientation.Horizontal){
            contents += new Label( UiStrings.en.pathToDatabase )
            contents += txtDatabasePath
            
            contents += Button ( UiStrings.en.btnOpenFolder ){

                file.fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly

                val result = file.showDialog( null,  UiStrings.en.selectFolder )
                if (result == FileChooser.Result.Approve){
                    if (testForDatabaseFiles(file.selectedFile.getPath)){
                        txtDatabasePath.text = file.selectedFile.getPath
                    }
                } else None
            }
        } // end Source Path

        contents += Swing.VStrut(10)

        // Destination Path
        contents += new BoxPanel(Orientation.Horizontal){
            contents += new Label( UiStrings.en.pathToOutput )
            contents += txtOutputPath

            contents += Button ( UiStrings.en.btnOpenFolder ){

                file.fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly

                val result = file.showDialog( null,  UiStrings.en.selectFolder )
                if (result == FileChooser.Result.Approve){
                    txtOutputPath.text = file.selectedFile.getPath
                } else None
            }
        } // end Destination Path

        contents += Swing.VStrut(10)

        contents += exportButton
        border = Swing.EmptyBorder(10)

    }

    def testForDatabaseFiles(path: String): Boolean = {
        val songDbFile = new File(path + "/Songs.db")

        if (!songDbFile.exists()){
            Dialog.showMessage(
                null, UiStrings.en.ewDatabaseNotFoundPrefix + songDbFile.getParent, null, Dialog.Message.Error,null)
            return false
        } else {
            Dialog.showMessage(null, UiStrings.en.ewDatabaseFound, null, Dialog.Message.Info,null)
        }

        true
    }

    def processRecords = {
        true
    }
}
