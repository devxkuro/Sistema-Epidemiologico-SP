public class Produto {
    protected int id;
    protected int codigo;
    protected String nome;
    protected double preco;
    protected int fornecedorId;

    public Produto(int id, int codigo, String nome, double preco, int fornecedorId) {
        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
        this.preco = preco;
        this.fornecedorId = fornecedorId;
    }
}