package org.example.api.listener;

import lombok.Getter;
import org.example.api.model.Video;
import org.springframework.context.ApplicationEvent;

// ============================================
// Evento personalizado para Visualización
// ============================================

@Getter
public class VisualizacionEvent extends ApplicationEvent {

    private final Video video;
    private final Long visualizacionId;

    public VisualizacionEvent(Object source, Video video, Long visualizacionId) {
        super(source);
        this.video = video;
        this.visualizacionId = visualizacionId;
    }
}
