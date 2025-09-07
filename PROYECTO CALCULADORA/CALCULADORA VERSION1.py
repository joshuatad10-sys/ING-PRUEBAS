import tkinter as tk

class Calculadora:
    def __init__(self, root):
        self.root = root
        self.root.title("Calculadora Básica")
        self.root.configure(bg="#161616")  # Fondo de la ventana (oscuro)
        self.expresion = ""
        self.historial = []
        root.resizable(0, 0)


        # Pantalla principal (caja de texto)
        self.pantalla = tk.Entry(
            root,
            font=("Consolas", 22, "bold"),  # Tipo de letra
            borderwidth=8,
            relief="sunken",
            justify="right",
            bg="#262627",   # Fondo gris oscuro
            fg="white",     # Texto blanco
            insertbackground="white"  # Cursor blanco
        )
        self.pantalla.grid(row=0, column=0, columnspan=4, ipadx=8, ipady=15, pady=10)

        # Estilo de botones / Diseño
        estilo_boton = {
            "font": ("Arial", 14, "bold"),
            "width": 8,
            "height": 2,
            "bd": 4,
            "relief": "raised",
            "bg": "#283C44",      # Fondo de botones
            "fg": "white",        # Texto blanco
            "activebackground": "#BBBEF0",  # Fondo al presionar
            "activeforeground": "black"     # Texto al presionar
        }

        # Lista de botones 
        botones = [
            ("7",1,0), ("8",1,1), ("9",1,2), ("/",1,3),
            ("4",2,0), ("5",2,1), ("6",2,2), ("*",2,3),
            ("1",3,0), ("2",3,1), ("3",3,2), ("-",3,3),
            ("0",4,0), (".",4,1), ("=",4,2), ("+",4,3),
            ("C",5,0), ("AC",5,1), ("Historial",5,2,2)
        ]

        # Botones con estilo personalizado 
        for (texto, fila, col, cs) in [(b[0], b[1], b[2], b[3] if len(b)>3 else 1) for b in botones]:
            tk.Button(
                root,
                text=texto,
                command=lambda t=texto:self.click(t),
                **estilo_boton
            ).grid(row=fila, column=col, columnspan=cs, padx=5, pady=5)

    def click(self, tecla):
        if tecla == "=":
            try:
                resultado = str(round(eval(self.expresion), 2))
                self.historial.append(self.expresion + " = " + resultado)
                self.pantalla.delete(0, tk.END)
                self.pantalla.insert(tk.END, resultado)
                self.expresion = resultado
            except:
                self.pantalla.delete(0, tk.END)
                self.pantalla.insert(tk.END, "Error")
                self.expresion = ""
        elif tecla == "C":
            self.expresion = self.expresion[:-1]
            self.pantalla.delete(0, tk.END)
            self.pantalla.insert(tk.END, self.expresion)
        elif tecla == "AC":
            self.expresion = ""
            self.pantalla.delete(0, tk.END)
        elif tecla == "Historial":
            self.mostrar_historial()
        else:
            self.expresion += tecla
            self.pantalla.delete(0, tk.END)
            self.pantalla.insert(tk.END, self.expresion)

    def mostrar_historial(self):
        ventana = tk.Toplevel(self.root)
        ventana.title("Historial")
        ventana.configure(bg="#283C44")
        for i, operacion in enumerate(self.historial, start=1):
            tk.Label(ventana, text=operacion, font=("Arial", 12), bg="#283C44", fg="white").pack()

# Ejecutar calculadora
if __name__ == "__main__":
    root = tk.Tk()
    app = Calculadora(root)
    root.mainloop()
