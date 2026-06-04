# Robot Supurge Simulasyonu

JavaFX ile gelistirilmis akilli supurge simulasyonu.

## Ozellikler

- Karanlik kontrol paneli ve oda gorunumu
- Kir ekleme, mobilya ekleme ve silgi modlari
- Toz, sivi ve leke icin farkli temizlik suresi ve pil maliyeti
- Rastgele, Spiral ve Akilli BFS temizlik algoritmalari
- Dusuk pilde otomatik sarja donus
- Manuel pil seviyesi guncelleme
- Gercek zamanli konum, yon, batarya, temizlenen alan, kalan kir ve sure gosterimi
- Olay gunlugu

## Calistirma

Maven kuruluysa:

```bash
mvn javafx:run
```

Maven yoksa IntelliJ icinden `MainApp` sinifini calistirabilirsiniz.

## Kullanim

1. `Kir Ekle` modunu acin ve zeminde bir hucreye tiklayin.
2. `Mobilya Ekle` ile engel ekleyin.
3. Algoritmayi secin.
4. `Baslat` ile simulasyonu calistirin.
5. `Istasyona Don` ile robota manuel sarj komutu verin.
6. `Bataryayi Uygula` ile pil seviyesini elle degistirin.
