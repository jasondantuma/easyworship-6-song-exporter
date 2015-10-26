/**
 * Created by Jason Dantuma on 2015/10/26.
 */

import swing._

class MainUi extends MainFrame {

    var textEntry = new TextField()

    title = "EasyWorship 6 Song Exporter"

    contents = new BoxPanel(Orientation.Vertical){
        contents += new BoxPanel(Orientation.Horizontal){
            contents += new Label("Path to database files: ")
            contents += textEntry
        }

        contents += Button ("Export Songs"){
            textEntry.revalidate()
            getText()
        }
    }

    def getText() {

        println(textEntry.text)

    }
}
