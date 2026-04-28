import {
  booleanAttribute,
  Component,
  EventEmitter,
  forwardRef,
  Input,
  OnChanges,
  Output,
  SimpleChanges
} from '@angular/core';
import { NgClass, NgIf } from '@angular/common';
import {
  AbstractControl,
  ControlValueAccessor,
  NG_VALIDATORS,
  NG_VALUE_ACCESSOR,
  ValidationErrors,
  Validator
} from '@angular/forms';
import { LocalizeComponent } from '../localize/localize.component';

let nextBooleanFieldId = 0;

@Component({
  standalone: true,
  selector: 'boolean-field',
  templateUrl: './boolean-field.component.html',
  styleUrls: ['./boolean-field.component.css'],
  imports: [NgClass, NgIf, LocalizeComponent],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => BooleanFieldComponent),
      multi: true
    },
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => BooleanFieldComponent),
      multi: true
    }
  ]
})
export class BooleanFieldComponent implements ControlValueAccessor, Validator, OnChanges {

  @Input() value: boolean = false;

  @Input({ transform: booleanAttribute })
  disabled: boolean = false;

  /**
   * When true, this field is invalid unless checked.
   *
   * Supports both:
   *   <boolean-field [required]="true">
   *   <boolean-field required>
   */
  @Input({ transform: booleanAttribute })
  required: boolean = false;

  @Input() requiredMessage: string = 'This field is required.';

  /**
   * Optional id. If not supplied, one is auto-generated.
   */
  @Input() id: string = `boolean-field-${nextBooleanFieldId++}`;

  /**
   * Localization key used to localize in the component template.
   */
  @Input() localizeLabelKey: string = '';

  /**
   * Raw string label.
   */
  @Input() label: string = '';

  @Input({ transform: booleanAttribute })
  inline: boolean = false;

  @Output() public valueChange = new EventEmitter<boolean>();

  touched: boolean = false;

  private onChange: (value: boolean) => void = () => {};
  private onTouched: () => void = () => {};
  private onValidatorChange: () => void = () => {};

  get requiredErrorId(): string {
    return `${this.id}-required-error`;
  }

  get invalidRequired(): boolean {
    return this.required && this.value !== true;
  }

  get showRequiredError(): boolean {
    return this.invalidRequired && this.touched && !this.disabled;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['required'] || changes['value'] || changes['disabled']) {
      this.onValidatorChange();
    }
  }

  toggle(): void {
    if (this.disabled) {
      return;
    }

    this.setValue(!this.value);
    this.markTouched();
  }

  markTouched(): void {
    if (!this.touched) {
      this.touched = true;
    }

    this.onTouched();
  }

  private setValue(value: boolean): void {
    this.value = value;

    this.valueChange.emit(this.value);
    this.onChange(this.value);
    this.onValidatorChange();
  }

  // ---------------------------------------------------------------------------
  // ControlValueAccessor
  // ---------------------------------------------------------------------------

  writeValue(value: boolean | null | undefined): void {
    this.value = value === true;
    this.onValidatorChange();
  }

  registerOnChange(fn: (value: boolean) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
    this.onValidatorChange();
  }

  // ---------------------------------------------------------------------------
  // Validator
  // ---------------------------------------------------------------------------

  validate(control: AbstractControl): ValidationErrors | null {
    if (!this.required || this.disabled) {
      return null;
    }

    return control.value === true ? null : { required: true };
  }

  registerOnValidatorChange(fn: () => void): void {
    this.onValidatorChange = fn;
  }
}