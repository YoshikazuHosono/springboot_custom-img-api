package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private ResourceLoader resourceLoader;

    @GetMapping(value = "/custom_img/{name}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> hello(@PathVariable String name) throws Exception {

        // 画像テンプレート読み込み
        Resource imageResource = resourceLoader.getResource("classpath:/img/template.png");
        // フォントファイル読み込み
        Resource fontResource = resourceLoader.getResource("classpath:/font/ipag.ttf");

        // フォント登録
        Font font = Font.createFont(Font.TRUETYPE_FONT, fontResource.getInputStream());
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);

        // FIXME 使用可能フォントを標準出力
        Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()).forEach(availableFont -> {
            System.out.println(availableFont.getName());
        });

        // 画像読み込み
        BufferedImage bufferedImage = ImageIO.read(imageResource.getInputStream());

        // 画像データの、画板みたいなオブジェクトを取り出す
        Graphics g = bufferedImage.getGraphics();

        // 書き込んだ文字にアンチエイリアスを効かせるための設定
        // これをやらないと、描画された文字がカクカクになる
        // FIXME もっとましな書き方はないのか、、、instanceof嫌い、、、
        if (g instanceof Graphics2D) {
            Graphics2D gg = ((Graphics2D) g);
            gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        // 画像ファイルに文字を書き込む
        g.setFont(new Font(font.getName(), Font.PLAIN, 50));
        g.setColor(new Color(255, 255, 255));
        g.drawString("hello " + name + "!", 100, 100);

        // 画像オブジェクトをバイト配列に変換
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedOutputStream os = new BufferedOutputStream(bos);
        ImageIO.write(bufferedImage, "png", os);
        byte[] b = bos.toByteArray();

        // レスポンスオブジェクト作成
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(b.length);

        return new ResponseEntity<>(b, headers, HttpStatus.OK);
    }

}
