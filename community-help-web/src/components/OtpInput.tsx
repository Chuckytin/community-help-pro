// Eliminada la importación de useRef que no se usa
interface OtpInputProps {
    /** Se llama con los 6 dígitos cuando están todos completos */
    onComplete?: (otp: string) => void;
    /** Referencia externa para leer los valores al enviar */
    inputRefs: React.MutableRefObject<HTMLInputElement[]>;
}

/**
 * Componente de entrada OTP de 6 dígitos.
 * - Avanza automáticamente al siguiente input al escribir
 * - Retrocede al anterior al pulsar Backspace
 * - Soporta pegar el código completo
 */
const OtpInput = ({ onComplete, inputRefs }: OtpInputProps) => {

    /**
     * Al escribir un dígito:
     * 1. Filtra caracteres no numéricos
     * 2. Avanza al siguiente input
     * 3. Si es el último, llama onComplete con el código completo
     */
    const handleChange = (e: React.ChangeEvent<HTMLInputElement>, index: number) => {
        const value = e.target.value.replace(/\D/, "");
        e.target.value = value;

        if (value && index < 5) {
            inputRefs.current[index + 1]?.focus();
        }

        // Comprobar si todos los inputs tienen valor
        if (onComplete) {
            const all = inputRefs.current.map(i => i?.value || "").join("");
            if (all.length === 6) onComplete(all);
        }
    };

    /**
     * Al pulsar Backspace en un input vacío, vuelve al anterior.
     */
    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>, index: number) => {
        if (e.key === "Backspace" && !e.currentTarget.value && index > 0) {
            inputRefs.current[index - 1]?.focus();
        }
    };

    /**
     * Al pegar texto:
     * 1. Previene el comportamiento por defecto
     * 2. Distribuye cada carácter en su input correspondiente
     * 3. Foca el último input rellenado
     */
    const handlePaste = (e: React.ClipboardEvent) => {
        e.preventDefault();
        const paste = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, 6).split("");
        paste.forEach((digit, i) => {
            if (inputRefs.current[i]) inputRefs.current[i].value = digit;
        });
        inputRefs.current[Math.min(paste.length, 5)]?.focus();

        if (onComplete && paste.length === 6) {
            onComplete(paste.join(""));
        }
    };

    return (
        <div className="flex gap-2 justify-center" onPaste={handlePaste}>
            {[...Array(6)].map((_, i) => (
                <input
                    key={i}
                    type="text"
                    inputMode="numeric"
                    maxLength={1}
                    ref={(el) => { if (el) inputRefs.current[i] = el; }}
                    onChange={(e) => handleChange(e, i)}
                    onKeyDown={(e) => handleKeyDown(e, i)}
                    className="w-11 h-14 text-center text-xl font-semibold rounded-lg border-(--border) bg-(--bg) text-(--text-h) focus:outline-none focus:border-(--accent) focus:ring-2 focus:ring-(--accent-bg) transition-all"
                />
            ))}
        </div>
    );
};

export default OtpInput;