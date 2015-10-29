package za.co.dantuma.jason.ewexport

import javax.swing.UIManager

/**
 * Created by Jason Dantuma on 2015/10/26.
 */



object EasyWorship6SongExporter {
    def main(args: Array[String]) {


        try
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)

        val ui = new MainUi
        ui.visible = true
    }
}
