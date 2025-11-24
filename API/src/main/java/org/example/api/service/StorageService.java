package org.example.api.service;

import org.example.api.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${storage.location:uploads}")
    private String storageLocation;

    @Value("${server.base-url:http://localhost:8080}")
    private String baseUrl;

    public String guardarVideo(MultipartFile archivo) {
        validarArchivoVideo(archivo);
        return guardarArchivo(archivo, "videos");
    }

    public String guardarImagen(MultipartFile archivo) {
        validarArchivoImagen(archivo);
        return guardarArchivo(archivo, "imagenes");
    }

    private String guardarArchivo(MultipartFile archivo, String carpeta) {
        try {
            // Crear directorio si no existe
            Path directorioBase = Paths.get(storageLocation, carpeta);
            if (!Files.exists(directorioBase)) {
                Files.createDirectories(directorioBase);
            }

            // Generar nombre único para el archivo
            String nombreOriginal = archivo.getOriginalFilename();
            String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
            String nombreArchivo = UUID.randomUUID().toString() + extension;

            // Guardar archivo
            Path rutaArchivo = directorioBase.resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

            // Retornar URL del archivo
            return baseUrl + "/files/" + carpeta + "/" + nombreArchivo;

        } catch (IOException e) {
            throw new BadRequestException("Error al guardar el archivo: " + e.getMessage());
        }
    }

    public void eliminarArchivo(String url) {
        try {
            // Extraer ruta del archivo desde la URL
            String[] partes = url.split("/files/");
            if (partes.length < 2) {
                return;
            }

            Path rutaArchivo = Paths.get(storageLocation, partes[1]);
            Files.deleteIfExists(rutaArchivo);

        } catch (IOException e) {
            throw new BadRequestException("Error al eliminar el archivo: " + e.getMessage());
        }
    }

    private void validarArchivoVideo(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new BadRequestException("El archivo está vacío");
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new BadRequestException("El archivo debe ser un video");
        }

        // 500MB en bytes
        long tamañoMaximo = 500 * 1024 * 1024;
        if (archivo.getSize() > tamañoMaximo) {
            throw new BadRequestException("El video no puede exceder 500MB");
        }
    }

    private void validarArchivoImagen(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new BadRequestException("El archivo está vacío");
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("El archivo debe ser una imagen");
        }

        // 5MB en bytes
        long tamañoMaximo = 5 * 1024 * 1024;
        if (archivo.getSize() > tamañoMaximo) {
            throw new BadRequestException("La imagen no puede exceder 5MB");
        }
    }
}
