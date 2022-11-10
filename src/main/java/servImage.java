

import java.io.*;
import java.net.ServerSocket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class servImage {

    public static void main(String[] args) throws IOException {
        int visit = 0;
        ServerSocket servsock = new ServerSocket(12000);//серверсокет с указанным портом
            Pattern path = Pattern.compile("(/[^\s]*)");//паттерн пути
            Pattern exe = Pattern.compile("(\\.[^\s]*)"); //паттерн расширения
            //открываю потоки
            try (var socket = servsock.accept(); //сокет соединения с клиентом
                 var isr = new BufferedReader(new InputStreamReader(socket.getInputStream()));//принимаю поток
                 var out = new DataOutputStream(socket.getOutputStream())) {//вывожу поток
                String request = isr.readLine();//считываю входящую инфу
                //создаю переменные для обработки информации
                String filePath = "";
                String exeType = "";
                String contentType = "";
                //создаю объект матчер для поиска возможных совпадений сежду паттернами и поступившей инфой
                Matcher matcher1 = path.matcher(request);//создаю объект matcher1 типа Matcher на основе поступившей от клиента информации
                Matcher matcher2 = exe.matcher(request);
                //создаю мап с парами расширение=контент-тип
                HashMap<String, String> mapContentType = new HashMap<>();
                mapContentType.put(".txt", "text/html");
                mapContentType.put(".html", "text/html");
                mapContentType.put(".gif", "image/gif");
                mapContentType.put(".jpeg", "image/jpeg");
                mapContentType.put(".png", "image/png");
                //ищу совпадения
                if (matcher1.find()) {
                    filePath = matcher1.group();//сохраняю путь
                }
                if (matcher2.find()) {
                    exeType = matcher2.group();//сохраняю тип расширения
                }
                //получаю доступ к парам мапы
                Set<Map.Entry<String, String>> exeMapContentType = mapContentType.entrySet();
                for (Map.Entry<String, String> pair : exeMapContentType) {
                    String key = pair.getKey();//получаю ключ
                    String value = pair.getValue();//получаю значение
                    if (key.equals(exeType)) {//если ключ совпадает с расширением
                        contentType = value;//то контент-типу присваиваем значение
                    }
                }
                System.out.println("Client " +  (++visit) + " accepted.");//информирую об обращении клиента
                File file = new File(filePath);//создаю файл  и передаю в него полученный путь
                try ( var inF = new FileInputStream(file) ) { //открываю поток для чтения файла
                    String tempBytes = getFileSizeKiloBytes(file);//вызываю статический метод перевода длины файла в объем файла
                    double tempDBytes = Double.parseDouble(tempBytes);// для конвертации
                    int qtyBytes;//инициирую переменную
                    //проверяю дабл-объем
                    if (tempDBytes < 1.00D) { //округляю (присваиваю новое значение)
                        qtyBytes = 1024;
                    } else {
                        qtyBytes = (int) tempDBytes;//перевожу в инт
                    }
                    byte[] bytes = new byte[qtyBytes];//создаю байт-массив для хранения информации из файла
                    String httpResponse =
                            "HTTP/1.0 200 OK\r\n" +
                            "Content-Type: " + contentType + "\r\n" +
                            "\r\n";
                    out.write(httpResponse.getBytes());//отправляю хттп-ответ
                    while ((inF.read(bytes)) > -1) {
                        out.write(bytes, 0, qtyBytes);//отправляю содержимое файла
                    }
                } catch (FileNotFoundException ex) {
                    System.out.println("Йок-макарёк, файла-та и нет: " + ex);//шуткую, если файла нет
                }
            }
        servsock.close();//закрываю сервер
        System.out.println("Server closed due to settings.");
    }
    private static String getFileSizeKiloBytes(File file) { //узнаю объем файла
        return (double) file.length()/1024 + "";
    }
}
