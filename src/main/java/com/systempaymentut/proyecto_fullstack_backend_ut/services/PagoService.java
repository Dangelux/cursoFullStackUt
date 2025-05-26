package com.systempaymentut.proyecto_fullstack_backend_ut.services;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.systempaymentut.proyecto_fullstack_backend_ut.entities.Estudiante;
import com.systempaymentut.proyecto_fullstack_backend_ut.entities.Pago;
import com.systempaymentut.proyecto_fullstack_backend_ut.enums.PagoStatus;
import com.systempaymentut.proyecto_fullstack_backend_ut.enums.TypePago;
import com.systempaymentut.proyecto_fullstack_backend_ut.repository.EstudianteRepository;

import com.systempaymentut.proyecto_fullstack_backend_ut.repository.PagoRepository;

import jakarta.transaction.Transactional;

@Service
// Para asegurar que los métodos de la clase se ejecuten dentro de una transacción
@Transactional
public class PagoService {

    // Inyección de dependencias de PagoRepository para interactuar con la BD de pagos
    @Autowired
    private PagoRepository pagoRepository;

    /* Inyección de dependencias de EstudianteRepository para obtener información de los
    estudiantes desde la BD */
    @Autowired
    private EstudianteRepository estudianteRepository;

    /*
     * Método para guardar el pago en la BD y almacenar un archivo PDF en el servidor
     *  @Param file: archivo PDF que se subirá al servidor
        @Param cantidad: monto del pago realizado
        @Param type: tipo de pago EFECTIVO, CHEQUE, TRANSFERENCIA, DEPOSITO*
        @Param date: fecha en la que se realiza el pago
        @Param codigoEstudiante: cod del estudiante que realiza el pago
        @return: objeto Pago guardado en la BD
        @throws IOException excepcion lanzada si ocurre un error al manejar el archivo pdf
    */
    public Pago savePago(MultipartFile file, double cantidad, TypePago type, LocalDate date, String codigoEstudiante) throws IOException{

        /*
         * Construir la ruta donde se guardará el archivo dentro del sistema
         * System.getProperty("user.home"): obtiene la ruta del directorio personal del usuario del actual SO
         * Paths.get: construir una ruta dentro del directorio personal en la carpeta "enset-data/pagos" 
        */

        Path folderPath = Paths.get(System.getProperty("user.home"), "enset-data", "pagos");

        // Verificar si la carpeta ya existe, si no la debe crear
        if(!Files.exists(folderPath)){
            Files.createDirectories(folderPath);
        }

        // Generación de un nombre único para el archivo usando UUID (Identificador Único Universal)
        String fileName = UUID.randomUUID().toString();

        // Construcción de la ruta completa del archivo agregando la extensión .pdf
        Path filePath = Paths.get(System.getProperty("user.home"), "enset-data", "pagos", fileName + ".pdf");

        // Se guarda el archivo recibido en la unicación especificada dentor del sistema de archivos
        Files.copy(file.getInputStream(), filePath);

        // Búsqueda del estudiante que realiza el pago con su código
        Estudiante estudiante = estudianteRepository.findByCodigo(codigoEstudiante);

        // Creación de un nuevo objeto Pago utlizando el patrón de diseño builder
        Pago pago = Pago.builder()
            .type(type)
            .status(PagoStatus.CREADO) // Estado inicial del pago
            .fecha(date)
            .estudiante(estudiante)
            .cantidad(cantidad)
            .file(filePath.toUri().toString()) // Ruta del archivo pdf almacenado
            .build(); // Construcción final del objeto Pago

        return pagoRepository.save(pago);

    }

    public byte[] getArchivoById(Long pagoId) throws IOException{

        // Busca un objeto Pago en la BD por su ID
        // Pago pago = pagoRepository.findById(pagoId).get();
        Pago pago = pagoRepository.findById(pagoId).get();

        /*
         * pago.getFile: obtiene la URI del archivo guardado como una cadena de texto
         * URI.create: convierte la cadena de texto en un objeto URI
         * pathOf: convierte la URI en un path(carpeta) para poder acceder al archivo
         * Files.readAllBytes: lee el contenido del archivo y lo va a devolver en un array de bytes
         * 
         * Esto permite obtener el contenido del archivo para su posterior uso, por ejemplo, descargarlo.
        */

        return Files.readAllBytes(Path.of(URI.create(pago.getFile())));
    }

    public Pago actualizarPagoPorStatus(PagoStatus status, Long id){

        // Busca un objeto Pago en la BD por su ID
        Pago pago = pagoRepository.findById(id).get();

        // Actualiza el estado del pago (validado o rechazado)
        pago.setStatus(status);

        // Guarda el objeto Pago actualizado en la BD y lo devuelve
        return pagoRepository.saveAll(pago);
    }

}
