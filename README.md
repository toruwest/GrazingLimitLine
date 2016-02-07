![スクリーンショット](https://raw.githubusercontent.com/toruwest/GrazingLimitLine/master/GrazingLimitLine-screenshot.png)

### このソフトについて

このソフトは、星食の限界線データを用いて、地図上で観測地の候補を検討するために使います。
従来は、同じ目的で、鈴木寿氏提供の<a href=\"http://www2.wbs.ne.jp/~spica/Grazing/2015/densi/2015GrazingMap.htm\">Webアプリ</a>が使われていましたが、これが依存している国土地理院のサービスが停止したため使えなくなりましたので、代わりに使えるソフトが必要だろうということで、作りました。

利用にあたっては、星食限界線のデータファイルが必要となります。月縁図についても、もしあれば表示します。

このソフトで観測地としてサポートしているのは、日本国内だけとなります。(ソフトの利用自体は海外在住の方でも問題ありません)

このアプリケーションソフトウェアは、無償で利用できます。

### ダウンロード

GrazingLimitLine.jar をクリックすると、"This file has been truncated, but you can view the full file.”と書かれたページになります。
これの”View Raw”を右クリックすると、GrazingLimitLine.jarというファイルのダウンロードが始まります。
これが実行ファイルです。インストーラーなどは付属していません。

なお、実行にあたり、Java Runtime Environment (JRE)が必要となりますので、以下からダウンロード・インストールしておいてください。

　http://www.oracle.com/technetwork/java/javase/downloads/index.html

"GrazingLimitLineの使い方.pdf"についても、クリックするとブラウザ内で表示されますが、日本語の文字が正しく表示されないようですので、ダウンロードして見た方がいいと思います。
クリックして移動する先の画面の右上に"Raw", "History"と並んでいる、"Raw"の方をクリックするとダウンロードできます。

### サポートしているOS

動作確認を行っているのはWindows 7とOS X 10.10 (Yosemite、こちらがメイン)です。
Windows 8, 10や、Linux系のOSでも動くはずですが、確認していません。

### 限界線データおよび月縁図について

利用にあたっては、星食限界線のデータファイルが別途必要となります。月縁図についても、もしあれば表示します。
これらのファイルは、別途、(http://www2.wbs.ne.jp/~spica/index.files/Page441.htm "鈴木氏のホームページ")から入手してください。

### 起動方法

 Windowsではエクスプローラー、マッキントッシュではファインダーで、このファイルをダブルクリックして起動します。

コマンドプロンプトあるいはターミナルから起動するには、このファイルがあるフォルダーに移動(cd)し、"java -jar GrazingLimitLine.jar"と入力します。

星食限界線のデータファイルを格納するフォルダーは、起動後に表示されるダイアログに記してあります。


