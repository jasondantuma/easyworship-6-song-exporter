package za.co.dantuma.jason.ewexport

import javax.swing.UIManager
import javax.swing.plaf.nimbus.NimbusLookAndFeel

/**
 * Created by Jason Dantuma on 2015/10/26.
 */



object EasyWorship6SongExporter {
    def main(args: Array[String]) {

        try
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
        catch {
            case e: Exception =>
                println("Could not get system look and feel, falling back to Nimbus")
                UIManager.setLookAndFeel(new NimbusLookAndFeel)
        }

        val ui = new MainUi
        ui.visible = true
    }
}
