import { Directive, ElementRef, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { Participant, Track, TrackPublication } from 'livekit-client';

/**
 * Anexa o primeiro VideoTrack disponível de um participante LiveKit
 * ao elemento <video> host. Reanexa quando o participante muda.
 *
 * Uso:
 *   <video appLivekitVideo [participant]="p" autoplay playsinline [muted]="isLocal"></video>
 */
@Directive({
  selector: '[appLivekitVideo]',
  standalone: true
})
export class LivekitVideoDirective implements OnChanges, OnDestroy {
  @Input('appLivekitVideo') participant: Participant | null = null;

  private attachedTrack: Track | null = null;

  constructor(private el: ElementRef<HTMLVideoElement>) {}

  ngOnChanges(_changes: SimpleChanges): void {
    this.updateTrack();
  }

  ngOnDestroy(): void {
    this.detach();
  }

  /** Recalcula e reanexa o track de vídeo do participante, se houver. */
  updateTrack(): void {
    const p = this.participant;
    if (!p) {
      this.detach();
      return;
    }
    const pub = Array.from(p.videoTrackPublications.values()).find(
      (pb: TrackPublication) => pb.track != null && !pb.isMuted
    );
    const track = pub?.track ?? null;

    if (track === this.attachedTrack) return;

    this.detach();

    if (track && track.kind === Track.Kind.Video) {
      try {
        track.attach(this.el.nativeElement);
        this.attachedTrack = track;
      } catch {
        /* ignore */
      }
    }
  }

  private detach(): void {
    if (this.attachedTrack) {
      try {
        this.attachedTrack.detach(this.el.nativeElement);
      } catch {
        /* ignore */
      }
      this.attachedTrack = null;
    }
  }
}
