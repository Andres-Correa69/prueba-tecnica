import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Componente raíz. Su única responsabilidad es alojar el `<router-outlet>`;
 * el resto del layout se decide en cada página (auth layout para
 * login/register, shell con navbar para `/cars`). Esta separación evita
 * que el componente raíz conozca detalles específicos de cada pantalla.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<router-outlet />`,
})
export class AppComponent {}
