import javax.swing.UIManager

/**
 * Created by Jason Dantuma on 2015/10/26.
 */



object EasyWorship6SongExporter {
    def main(args: Array[String]) {

        try {
            // Set the Look and Feel of the application to the operating
            // system's look and feel.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }

        val ui = new MainUi
        ui.visible = true
    }
}
