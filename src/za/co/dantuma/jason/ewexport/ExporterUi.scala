package za.co.dantuma.jason.ewexport

/**
 * Created by Jason Dantuma on 2015/10/28.
 */

import scala.swing._

trait ExporterUi extends MainFrame {
    def setProgressRecordCount(count: Long)
    def updateProgress(value: Int)
    def exportComplete()
}
