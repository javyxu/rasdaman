# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'wcps_client_dialog_base.ui'
#
# Created: Mon Oct 13 11:27:23 2014
#      by: PyQt4 UI code generator 4.9.1
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    _fromUtf8 = lambda s: s

class Ui_WCPSClient(object):
    def setupUi(self, WCPSClient):
        WCPSClient.setObjectName(_fromUtf8("WCPSClient"))
        WCPSClient.resize(714, 555)
        self.tabWidget_WCPSClient = QtGui.QTabWidget(WCPSClient)
        self.tabWidget_WCPSClient.setGeometry(QtCore.QRect(10, 10, 701, 531))
        self.tabWidget_WCPSClient.setObjectName(_fromUtf8("tabWidget_WCPSClient"))
        self.tab_Serv = QtGui.QWidget()
        self.tab_Serv.setObjectName(_fromUtf8("tab_Serv"))
        self.groupBox = QtGui.QGroupBox(self.tab_Serv)
        self.groupBox.setGeometry(QtCore.QRect(10, 10, 671, 101))
        self.groupBox.setObjectName(_fromUtf8("groupBox"))
        self.label = QtGui.QLabel(self.groupBox)
        self.label.setGeometry(QtCore.QRect(20, 20, 67, 17))
        self.label.setObjectName(_fromUtf8("label"))
        self.cmbConnections_Serv = QtGui.QComboBox(self.groupBox)
        self.cmbConnections_Serv.setGeometry(QtCore.QRect(20, 40, 651, 27))
        self.cmbConnections_Serv.setObjectName(_fromUtf8("cmbConnections_Serv"))
        self.btnConnectServer_Serv = QtGui.QPushButton(self.groupBox)
        self.btnConnectServer_Serv.setGeometry(QtCore.QRect(20, 70, 141, 27))
        self.btnConnectServer_Serv.setObjectName(_fromUtf8("btnConnectServer_Serv"))
        self.btnNew_Serv = QtGui.QPushButton(self.groupBox)
        self.btnNew_Serv.setGeometry(QtCore.QRect(170, 70, 71, 27))
        self.btnNew_Serv.setObjectName(_fromUtf8("btnNew_Serv"))
        self.btnEdit_Serv = QtGui.QPushButton(self.groupBox)
        self.btnEdit_Serv.setGeometry(QtCore.QRect(250, 70, 71, 27))
        self.btnEdit_Serv.setObjectName(_fromUtf8("btnEdit_Serv"))
        self.btnDelete_Serv = QtGui.QPushButton(self.groupBox)
        self.btnDelete_Serv.setGeometry(QtCore.QRect(330, 70, 81, 27))
        self.btnDelete_Serv.setObjectName(_fromUtf8("btnDelete_Serv"))
        self.label_2 = QtGui.QLabel(self.tab_Serv)
        self.label_2.setGeometry(QtCore.QRect(30, 120, 231, 17))
        font = QtGui.QFont()
        font.setBold(True)
        font.setItalic(True)
        font.setWeight(75)
        self.label_2.setFont(font)
        self.label_2.setObjectName(_fromUtf8("label_2"))
        self.textBrowser_Serv = QtGui.QTextBrowser(self.tab_Serv)
        self.textBrowser_Serv.setGeometry(QtCore.QRect(30, 140, 651, 311))
        self.textBrowser_Serv.setObjectName(_fromUtf8("textBrowser_Serv"))
        self.btnClose_Serv = QtGui.QPushButton(self.tab_Serv)
        self.btnClose_Serv.setGeometry(QtCore.QRect(580, 460, 99, 27))
        self.btnClose_Serv.setObjectName(_fromUtf8("btnClose_Serv"))
        self.tabWidget_WCPSClient.addTab(self.tab_Serv, _fromUtf8(""))
        self.tab_PC = QtGui.QWidget()
        self.tab_PC.setEnabled(False)
        self.tab_PC.setObjectName(_fromUtf8("tab_PC"))
        self.groupBox_2 = QtGui.QGroupBox(self.tab_PC)
        self.groupBox_2.setGeometry(QtCore.QRect(10, 10, 681, 441))
        self.groupBox_2.setObjectName(_fromUtf8("groupBox_2"))
        self.plainTextEdit_PC = QtGui.QPlainTextEdit(self.groupBox_2)
        self.plainTextEdit_PC.setGeometry(QtCore.QRect(3, 27, 671, 361))
        self.plainTextEdit_PC.setObjectName(_fromUtf8("plainTextEdit_PC"))
        self.label_3 = QtGui.QLabel(self.groupBox_2)
        self.label_3.setGeometry(QtCore.QRect(0, 390, 411, 17))
        font = QtGui.QFont()
        font.setBold(True)
        font.setWeight(75)
        self.label_3.setFont(font)
        self.label_3.setObjectName(_fromUtf8("label_3"))
        self.lineEdit_path = QtGui.QLineEdit(self.groupBox_2)
        self.lineEdit_path.setGeometry(QtCore.QRect(0, 410, 631, 27))
        self.lineEdit_path.setObjectName(_fromUtf8("lineEdit_path"))
        self.toolButton_path = QtGui.QToolButton(self.groupBox_2)
        self.toolButton_path.setGeometry(QtCore.QRect(640, 410, 24, 25))
        self.toolButton_path.setObjectName(_fromUtf8("toolButton_path"))
        self.pushButton_PC = QtGui.QPushButton(self.tab_PC)
        self.pushButton_PC.setGeometry(QtCore.QRect(550, 450, 131, 41))
        self.pushButton_PC.setObjectName(_fromUtf8("pushButton_PC"))
        self.btnClose_PC = QtGui.QPushButton(self.tab_PC)
        self.btnClose_PC.setGeometry(QtCore.QRect(440, 460, 99, 27))
        self.btnClose_PC.setObjectName(_fromUtf8("btnClose_PC"))
        self.tabWidget_WCPSClient.addTab(self.tab_PC, _fromUtf8(""))
        self.tab_Help = QtGui.QWidget()
        self.tab_Help.setObjectName(_fromUtf8("tab_Help"))
        self.textBrowser_2 = QtGui.QTextBrowser(self.tab_Help)
        self.textBrowser_2.setGeometry(QtCore.QRect(10, 11, 681, 441))
        self.textBrowser_2.setObjectName(_fromUtf8("textBrowser_2"))
        self.btnClose_Help = QtGui.QPushButton(self.tab_Help)
        self.btnClose_Help.setGeometry(QtCore.QRect(590, 460, 99, 27))
        self.btnClose_Help.setObjectName(_fromUtf8("btnClose_Help"))
        self.tabWidget_WCPSClient.addTab(self.tab_Help, _fromUtf8(""))
        self.tab_About = QtGui.QWidget()
        self.tab_About.setObjectName(_fromUtf8("tab_About"))
        self.textBrowser_3 = QtGui.QTextBrowser(self.tab_About)
        self.textBrowser_3.setGeometry(QtCore.QRect(10, 10, 681, 441))
        self.textBrowser_3.setObjectName(_fromUtf8("textBrowser_3"))
        self.btnClose_About = QtGui.QPushButton(self.tab_About)
        self.btnClose_About.setGeometry(QtCore.QRect(590, 460, 99, 27))
        self.btnClose_About.setObjectName(_fromUtf8("btnClose_About"))
        self.tabWidget_WCPSClient.addTab(self.tab_About, _fromUtf8(""))

        self.retranslateUi(WCPSClient)
        self.tabWidget_WCPSClient.setCurrentIndex(2)
        QtCore.QObject.connect(self.btnClose_Serv, QtCore.SIGNAL(_fromUtf8("clicked()")), WCPSClient.close)
        QtCore.QObject.connect(self.btnClose_About, QtCore.SIGNAL(_fromUtf8("clicked()")), WCPSClient.close)
        QtCore.QObject.connect(self.btnClose_Help, QtCore.SIGNAL(_fromUtf8("clicked()")), WCPSClient.close)
        QtCore.QObject.connect(self.btnClose_PC, QtCore.SIGNAL(_fromUtf8("clicked()")), WCPSClient.close)
        QtCore.QObject.connect(self.btnConnectServer_Serv, QtCore.SIGNAL(_fromUtf8("clicked()")), WCPSClient.connectServer)
        QtCore.QObject.connect(self.btnNew_Serv, QtCore.SIGNAL(_fromUtf8("clicked()")), WCPSClient.newServer)
        QtCore.QObject.connect(self.btnEdit_Serv, QtCore.SIGNAL(_fromUtf8("clicked()")), WCPSClient.editServer)
        QtCore.QObject.connect(self.btnDelete_Serv, QtCore.SIGNAL(_fromUtf8("clicked()")), WCPSClient.deleteServer)
        QtCore.QObject.connect(self.pushButton_PC, QtCore.SIGNAL(_fromUtf8("clicked()")), WCPSClient.exeProcessCoverage)
        QtCore.QObject.connect(self.toolButton_path, QtCore.SIGNAL(_fromUtf8("clicked()")), WCPSClient.get_outputLoc)
        QtCore.QMetaObject.connectSlotsByName(WCPSClient)

    def retranslateUi(self, WCPSClient):
        WCPSClient.setWindowTitle(QtGui.QApplication.translate("WCPSClient", "WCPS Client", None, QtGui.QApplication.UnicodeUTF8))
        self.groupBox.setTitle(QtGui.QApplication.translate("WCPSClient", "Server Connections:", None, QtGui.QApplication.UnicodeUTF8))
        self.label.setText(QtGui.QApplication.translate("WCPSClient", "Server", None, QtGui.QApplication.UnicodeUTF8))
        self.btnConnectServer_Serv.setText(QtGui.QApplication.translate("WCPSClient", "Connect to Server", None, QtGui.QApplication.UnicodeUTF8))
        self.btnNew_Serv.setText(QtGui.QApplication.translate("WCPSClient", "New", None, QtGui.QApplication.UnicodeUTF8))
        self.btnEdit_Serv.setText(QtGui.QApplication.translate("WCPSClient", "Edit", None, QtGui.QApplication.UnicodeUTF8))
        self.btnDelete_Serv.setText(QtGui.QApplication.translate("WCPSClient", "Delete", None, QtGui.QApplication.UnicodeUTF8))
        self.label_2.setText(QtGui.QApplication.translate("WCPSClient", "General Information / Errors etc.", None, QtGui.QApplication.UnicodeUTF8))
        self.btnClose_Serv.setText(QtGui.QApplication.translate("WCPSClient", "Close", None, QtGui.QApplication.UnicodeUTF8))
        self.tabWidget_WCPSClient.setTabText(self.tabWidget_WCPSClient.indexOf(self.tab_Serv), QtGui.QApplication.translate("WCPSClient", "Server/ Storage", None, QtGui.QApplication.UnicodeUTF8))
        self.groupBox_2.setTitle(QtGui.QApplication.translate("WCPSClient", "Queries : ", None, QtGui.QApplication.UnicodeUTF8))
        self.label_3.setText(QtGui.QApplication.translate("WCPSClient", "Local Storage Path", None, QtGui.QApplication.UnicodeUTF8))
        self.toolButton_path.setText(QtGui.QApplication.translate("WCPSClient", "...", None, QtGui.QApplication.UnicodeUTF8))
        self.pushButton_PC.setText(QtGui.QApplication.translate("WCPSClient", "ProcessCoverage", None, QtGui.QApplication.UnicodeUTF8))
        self.btnClose_PC.setText(QtGui.QApplication.translate("WCPSClient", "Close", None, QtGui.QApplication.UnicodeUTF8))
        self.tabWidget_WCPSClient.setTabText(self.tabWidget_WCPSClient.indexOf(self.tab_PC), QtGui.QApplication.translate("WCPSClient", "Process Coverage", None, QtGui.QApplication.UnicodeUTF8))
        self.textBrowser_2.setHtml(QtGui.QApplication.translate("WCPSClient", "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\" \"http://www.w3.org/TR/REC-html40/strict.dtd\">\n"
"<html><head><meta name=\"qrichtext\" content=\"1\" /><style type=\"text/css\">\n"
"p, li { white-space: pre-wrap; }\n"
"</style></head><body style=\" font-family:\'Ubuntu\'; font-size:11pt; font-weight:400; font-style:normal;\">\n"
"<p style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:22pt; font-weight:600;\">QgsWcpsClient1</span></p>\n"
"<hr width=\"100%\"/>\n"
"<p style=\" margin-top:16px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">Help - QgsWcpsClient1:   Instructions and Hints</span></p>\n"
"<p style=\" margin-top:14px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">Content:</span></p>\n"
"<ul style=\"margin-top: 0px; margin-bottom: 0px; margin-left: 0px; margin-right: 0px; -qt-list-indent: 1;\"><li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:12px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><a href=\"#Introduction\"><span style=\" text-decoration: underline; color:#0000ff;\">Introduction:</span></a>   What the QgsWcpsClient1 plugin is about    </li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><a href=\"#Getting Started\"><span style=\" text-decoration: underline; color:#0000ff;\">Getting Started:</span></a>   Initiating the QgsWcpsClient1 plugin    </li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><a href=\"#How To\"><span style=\" text-decoration: underline; color:#0000ff;\">How To:</span></a>   A detailed description of the available functionalities and settings    </li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><a href=\"#Hint\"><span style=\" text-decoration: underline; color:#0000ff;\">Hint - Erroneous behaviour</span></a>    </li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><a href=\"#Quick How To\"><span style=\" text-decoration: underline; color:#0000ff;\">Quick How To:</span></a>   A very short, stepwise description    </li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><a href=\"#Other Stuff\"><span style=\" text-decoration: underline; color:#0000ff;\">Other Stuff</span></a></li></ul>\n"
"<p style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><a name=\"Introduction\"></a><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">I</span><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">ntroduction:</span></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">This client is created to be used for abstract WCPS queries. This plugin mainly helps download results within QGis for any ProcessCoverage query and visualize it direcly in QGis(&gt;2.0). In case the results are not images, then the results are displayed in a dialog box. <br />In the GUI, there are two tabs. One for server and another for the query. Only after connecting the server, giving queries is possible. To process queries, a directory must be specified. </p>\n"
"<p style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><a name=\"Getting Started\"></a><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">G</span><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">etting Started:</span></p>\n"
"<ul style=\"margin-top: 0px; margin-bottom: 0px; margin-left: 0px; margin-right: 0px; -qt-list-indent: 1;\"><li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:12px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">The first thing which needs to be done is to define at least one server in the &quot;Server/Storage&quot; tab. Press the &quot;New&quot; button, and enter a (personal) &quot;Server Name&quot; and the WCPS Access &quot;Server URL&quot;. Press &quot;OK&quot; when done. All server entries are stored in the QgsWcpsClient1 plugin installation directory.    </li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">Make sure the provided WCPS Access &quot;Server URL&quot; is a valid page for posting WCPS queries    </li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">The supplied server information can be changed anytime utilizing the &quot;Edit&quot; button.    </li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">Any server entry can be deleted utilizing the &quot;Delete&quot; button&quot;.     </li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">When the server information has been provided, select the desired &quot;Server&quot; entry from the list and press the &quot;Connect to Server&quot; button.    </li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">You always need to be connected to a server before you can send requests!</li></ul>\n"
"<p style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><a name=\"How To\"></a><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">H</span><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">ow To:</span></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\';\">This section shortly describes a \'typical\' usage scenario for QgsWcpsClient1.</span></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">Server/Storage tab</span></p>\n"
"<ul style=\"margin-top: 0px; margin-bottom: 0px; margin-left: 0px; margin-right: 0px; -qt-list-indent: 1;\"><li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:12px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">The QgsWcpsClient1 plugin always starts with the &quot;Server/Storage&quot; tab.    </li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">Select the desired &quot;Server&quot; entry from the list and press the &quot;Connect to Server&quot; button. You always need to be connected to a server before you can send requests!</li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">Now, after connecting to a server, the ProcessCoverage tab is enabled and sending queries is possible. See ProcessCovereage below for more.        </li></ul>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">ProcessCoverage tab</span></p>\n"
"<ul style=\"margin-top: 0px; margin-bottom: 0px; margin-left: 0px; margin-right: 0px; -qt-list-indent: 1;\"><li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">The abstract query can be entered in the input text box.</li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">Before executing the query, the path to save the files needs to be specified. Please choose a valid path to save the intermediate files. </li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">After having a valid path, click on ProcessCoverage button. If the return object is an image, then it is added to the Canvas. Else, a dialogue will show the returned text values for the query. <br /></li></ul>\n"
"<p style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><br /></p>\n"
"<p style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><a name=\"Hint\"></a><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">H</span><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">int - Erroneous behaviour</span></p>\n"
"<ul style=\"margin-top: 0px; margin-bottom: 0px; margin-left: 0px; margin-right: 0px; -qt-list-indent: 1;\"><li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:12px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">In case you encounter an error (except server errors) or other strange behaviour, please close the QgsWcpsClient1 plugin, restart QGis, activate the \'Python Console\' from within QGis (Menu: Plugins -&gt; Python Console), reload the QgsWcpsClient1 plugin and repeat your previous actions. (Note: in case you have the \'Plugin Reloader\' plugin installed you just need to close the  QgsWcpsClient1 plugin, open the \'Python Console\' and reload the QgsWcpsClient1 plugin, before repeating your previous actions).    </li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">You should then get some additional information about the requests sent and error encountered. Please include this output in an error report to us.    </li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">This output also shows the requests sent to the servers. Please copy these requests and try them in a regular browser and check if they work there and also check the error messages returned from the server, before sending error reports.     </li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">In most cases problems will be due to server errors e.g. an optional parameter is not supported. Unfortunately some servers do not indicate this directly but just issue a general server error. So please try the request with differernt parameter settings before sending error reports.</li></ul>\n"
"<p style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><a name=\"Quick How To\"></a><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">Q</span><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">uick How To:</span></p>\n"
"<ol style=\"margin-top: 0px; margin-bottom: 0px; margin-left: 0px; margin-right: 0px; -qt-list-indent: 1;\"><li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:12px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">Select a WCPS server from the &quot;Server&quot; list and press the &quot;Connect to Server&quot; button.    </li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">In the ProcessCoverage tab, enter your query.</li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">Add a path to store intermediate files.     </li>\n"
"<li style=\" font-family:\'arial,helvetica,sans-serif\';\" style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">Press &quot;ProcessCoverage&quot; button to execute the query. </li></ol>\n"
"<p style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-family:\'arial,helvetica,sans-serif\';\"><br /></p>\n"
"<p style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><a name=\"Other Stuff\"></a><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">O</span><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">ther Stuff:</span></p>\n"
"<p style=\" margin-top:12px; margin-bottom:4px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">About</span></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\';\">General Information about:   Author / Company / Contact / Copyright / Acknowledgements / License.<br />Please see the separate &quot;About&quot; tab.</span></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\';\"> </span></p></body></html>", None, QtGui.QApplication.UnicodeUTF8))
        self.btnClose_Help.setText(QtGui.QApplication.translate("WCPSClient", "Close", None, QtGui.QApplication.UnicodeUTF8))
        self.tabWidget_WCPSClient.setTabText(self.tabWidget_WCPSClient.indexOf(self.tab_Help), QtGui.QApplication.translate("WCPSClient", "Help", None, QtGui.QApplication.UnicodeUTF8))
        self.textBrowser_3.setHtml(QtGui.QApplication.translate("WCPSClient", "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\" \"http://www.w3.org/TR/REC-html40/strict.dtd\">\n"
"<html><head><meta name=\"qrichtext\" content=\"1\" /><style type=\"text/css\">\n"
"p, li { white-space: pre-wrap; }\n"
"</style></head><body style=\" font-family:\'Ubuntu\'; font-size:11pt; font-weight:400; font-style:normal;\">\n"
"<p style=\" margin-top:18px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-size:xx-large; font-weight:600;\">QgsWcpsClient1</span></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:16pt; font-weight:600;\">An OGC WCPS 1.0 Client</span></p>\n"
"<p style=\"-qt-paragraph-type:empty; margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-size:16pt; font-weight:600;\"><br /></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">Author:</span><span style=\" font-family:\'arial,helvetica,sans-serif\';\">   Bidesh Thapaliya, Dimitar Misev</span></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">Company:</span><span style=\" font-family:\'arial,helvetica,sans-serif\';\">   </span><a href=\"http://www.eox.at\"><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600; text-decoration: underline; color:#0000ff;\">rasdaman GmbH</span></a> / Jacobs University Bremen</p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">Contact:</span><span style=\" font-family:\'arial,helvetica,sans-serif\';\">   misev at rasdaman dot com</span></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">Acknowledgements:</span><span style=\" font-family:\'arial,helvetica,sans-serif\';\">   The development of this QgsWcpsClient1 plugin (an OGC WCPS 1.0 Client) was partially financed by ESA (European Space Agency) under the ESA-GSTP DREAM/Delta-Dream project (991/C03-YC). The code was forked and accordingly adapted from the QgsWcsClient2.</span></p>\n"
"<p style=\"-qt-paragraph-type:empty; margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><br /></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">License: </span><span style=\" font-family:\'arial,helvetica,sans-serif\';\">    The MIT License (MIT)</span></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\';\">Copyright (c) 2014 rasdaman GmbH</span></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\';\">Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the &quot;Software&quot;), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:</span></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\';\">The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.</span></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\';\">THE SOFTWARE IS PROVIDED &quot;AS IS&quot;, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.</span></p>\n"
"<p style=\"-qt-paragraph-type:empty; margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><br /></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\'; font-weight:600;\">License: </span><span style=\" font-family:\'arial,helvetica,sans-serif\';\">    The MIT License (MIT)</span></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\';\">Copyright (c) 2014 EOX IT Services GmbH</span></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\';\">Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the &quot;Software&quot;), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:</span></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\';\">The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.</span></p>\n"
"<p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:\'arial,helvetica,sans-serif\';\">THE SOFTWARE IS PROVIDED &quot;AS IS&quot;, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.</span></p>\n"
"<p style=\"-qt-paragraph-type:empty; margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><br /></p></body></html>", None, QtGui.QApplication.UnicodeUTF8))
        self.btnClose_About.setText(QtGui.QApplication.translate("WCPSClient", "Close", None, QtGui.QApplication.UnicodeUTF8))
        self.tabWidget_WCPSClient.setTabText(self.tabWidget_WCPSClient.indexOf(self.tab_About), QtGui.QApplication.translate("WCPSClient", "About", None, QtGui.QApplication.UnicodeUTF8))
