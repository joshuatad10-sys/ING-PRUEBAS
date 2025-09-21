import tkinter as tk
from tkinter import ttk

class Calculadora:
    def __init__(self, root):
        self.root = root
        self.root.title("Calculadora V2.0 - Modern UI")
        self.root.geometry("400x550")
        self.root.configure(bg="#f0f2f5")
        self.expresion = ""
        self.historial = []
        self.memoria = 0.0

        # Estilo 
        style = ttk.Style()
        style.theme_use("clam")  # Tema base
        style.configure(
            "TButton",
            font=("Segoe UI", 14, "bold"),
            padding=10,
            relief="flat",
            background="#ffffff",
            foreground="#333333"
        )
        style.map(
            "TButton",
            background=[("active", "#c5bcbc")],
            foreground=[("active", "#000000")]
        )

        # Pantalla principal
        self.pantalla = tk.Entry(
            root,
            font=("Consolas", 24, "bold"),
            borderwidth=0,
            relief="flat",
            justify="right",
            bg="#ffffff",
            fg="#000000",
            state="readonly"
        )
        self.pantalla.pack(fill="x", padx=20, pady=20, ipady=15)

        # Contenedor de botones
        frame = tk.Frame(root, bg="#ffffff")
        frame.pack(expand=True)

        # Lista de botones
        botones = [
            ("7",1,0), ("8",1,1), ("9",1,2), ("/",1,3), ("MC",1,4),
            ("4",2,0), ("5",2,1), ("6",2,2), ("*",2,3), ("MR",2,4),
            ("1",3,0), ("2",3,1), ("3",3,2), ("-",3,3), ("M+",3,4),
            ("0",4,0), (".",4,1), ("=",4,2), ("+",4,3), ("M-",4,4),
            ("C",5,0), ("AC",5,1), ("Historial",5,2,3)
        ]

        # Colores especiales
        especiales = {"=": "#1c4952", "AC": "#3682f4", "C": "#0b2364", "Historial": "#525253"} 

        # Crear botones
        for (texto, fila, col, cs) in [(b[0], b[1], b[2], b[3] if len(b)>3 else 1) for b in botones]:
            btn = ttk.Button(frame, text=texto, command=lambda t=texto:self.click(t))
            btn.grid(row=fila, column=col, columnspan=cs, padx=5, pady=5, sticky="nsew")

            # Aplicar colores especiales
            if texto in especiales:
                btn.configure(style=f"{texto}.TButton")
                style.configure(
                    f"{texto}.TButton",
                    background=especiales[texto],
                    foreground="white"
                )
                style.map(
                    f"{texto}.TButton",
                    background=[("active", especiales[texto])],
                    foreground=[("active", "#ffffff")]
                )

        # Expandir columnas/filas para diseño responsivo
        for i in range(5):
            frame.grid_columnconfigure(i, weight=1)
        for i in range(1,6):
            frame.grid_rowconfigure(i, weight=1)

    def click(self, tecla):
        if tecla == "=":
            try:
                resultado = str(round(eval(self.expresion), 2))
                self.historial.append(self.expresion + " = " + resultado)
                self.expresion = resultado
                self.mostrar_en_pantalla(resultado)
            except:
                self.mostrar_en_pantalla("Error")
                self.expresion = ""
        elif tecla == "C":
            self.expresion = self.expresion[:-1]
            self.mostrar_en_pantalla(self.expresion)
        elif tecla == "AC":
            self.expresion = ""
            self.mostrar_en_pantalla("")
        elif tecla == "Historial":
            self.mostrar_historial()
        elif tecla == "M+":
            try:
                self.memoria += float(self.expresion)
                self.mostrar_en_pantalla(str(self.memoria))
            except:
                pass
        elif tecla == "M-":
            try:
                self.memoria -= float(self.expresion)
                self.mostrar_en_pantalla(str(self.memoria))
            except:
                pass
        elif tecla == "MC":
            self.memoria = 0.0
            self.mostrar_en_pantalla("0")
        elif tecla == "MR":
            self.expresion += str(self.memoria)
            self.mostrar_en_pantalla(self.expresion)
        else:
            self.expresion += tecla
            self.mostrar_en_pantalla(self.expresion)

    def mostrar_en_pantalla(self, texto):
        self.pantalla.config(state="normal")
        self.pantalla.delete(0, tk.END)
        self.pantalla.insert(tk.END, texto)
        self.pantalla.config(state="readonly")

    def mostrar_historial(self):
        ventana = tk.Toplevel(self.root)
        ventana.title("Historial")
        ventana.configure(bg="#ffffff")
        for i, operacion in enumerate(self.historial, start=1):
            tk.Label(ventana, text=operacion, font=("Segoe UI", 12), bg="#ffffff", fg="#333333").pack(pady=2)

# Ejecutar calculadora
if __name__ == "__main__":
    root = tk.Tk()
    app = Calculadora(root)
    root.mainloop()
