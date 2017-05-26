/**
 *
 * @author rgcoelho
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServidorTCP {
   public static void main(String args[]) throws IOException {
      GerenciarJogadores gerenciar = new GerenciarJogadores();
      GerenciarMoedas gerenciarMoedas = new GerenciarMoedas();
      Moeda aux = new Moeda(91238);
      gerenciarMoedas.moedas.add(aux);
      ServerSocket servidor = new ServerSocket(8989);
      criarMoedas(gerenciarMoedas);
      while (true) {
        Socket conexao = servidor.accept();
        new WorkerThread(conexao, gerenciar, gerenciarMoedas).start();
    }
   }
   public static void criarMoedas(GerenciarMoedas gerenciarMoedas){
		new Thread() {
			
			@Override
			public void run() {
                            try {
                                while(true){
                                    Random rand = new Random();
                                    Moeda aux = new Moeda(rand.nextInt(200000));
                                    gerenciarMoedas.moedas.add(aux);
                                    Thread.sleep(60 * 60);
                                }
                            } catch (InterruptedException ex) {
                                Logger.getLogger(ServidorTCP.class.getName()).log(Level.SEVERE, null, ex);
                            }
			}
		}.start();

	}

    private static class WorkerThread extends Thread {
        Socket socket;
        GerenciarJogadores gerenciar;
        GerenciarMoedas gerenciarMoedas;
        public WorkerThread(Socket conexao, GerenciarJogadores gerenciar, GerenciarMoedas gerenciarMoedas) {
            this.socket = conexao;
            this.gerenciar = gerenciar;
            this.gerenciarMoedas = gerenciarMoedas;
        }
        public int contemJogador(Jogador jogador){
            for(int i=0;i<gerenciar.jogadores.size();i++){
                        if(gerenciar.jogadores.get(i).getId().equals(jogador.getId())){
                        return i;
                        
                        }
            }
            return -1;
        }
        public int contemJogadorID(String id){
            for(int i=0;i<gerenciar.jogadores.size();i++){
                        if(gerenciar.jogadores.get(i).getId().equals(id)){
                        return i;
                        
                        }
            }
            return -1;
        }
        public int pegarID(){
            return gerenciar.jogadores.size()+1;
        }
        public void atualizarJogador(Jogador jogador){
            int numero = contemJogador(jogador);
            if(numero!=-1){
                
                         gerenciar.jogadores.set(numero, jogador);
                       
                       
                
            }else{
            gerenciar.jogadores.add(jogador);
            }
        }
        
        public void excluirJogador(String id){
           // int idd = Integer.parseInt(id);
            //idd--;
            gerenciar.jogadores.remove(contemJogadorID(id));
        }
        public void excluirMoeda(int id){
            for (int i=0;i<gerenciarMoedas.moedas.size();i++){
                if(gerenciarMoedas.moedas.get(i).getId() == id){
                    gerenciarMoedas.moedas.remove(i);
                }
            }
            
        }

        public void run() {

          int contador=0;
          PrintStream saida;
          Scanner entrada;
          boolean sair = false;
          String mensagem = "";
          System.out.println("Conexao estabelecida com: " + socket.getInetAddress().getHostAddress());
            try {
                //obtendo os fluxos de entrada e de saida
                saida = new PrintStream(socket.getOutputStream());
                entrada = new Scanner(socket.getInputStream());
            //enviando a mensagem abaixo ao cliente
            //saida.writeObject("Conexao estabelecida com sucesso...\n");
            do {//fica aqui ate' o cliente enviar a mensagem FIM
                   //obtendo a mensagem enviada pelo cliente
                   mensagem = (String) entrada.nextLine();
                   if(mensagem == ""){
                       System.out.println("Mensagem Vazia");
                   }
                   System.out.println("Cliente>> " + mensagem);
                   contador++;
                   String info[] = mensagem.split("\\+");
                   if("att".equals(info[0])){
                            String infoPlayer = info[1];
                            String infoo[] = infoPlayer.split(";");
                                Jogador aux = new Jogador(infoo[0],infoo[1]);
                                aux.setX(Integer.parseInt(infoo[2]));
                                aux.setY(Integer.parseInt(infoo[3]));
                                aux.setScore(Integer.parseInt(infoo[4]));
                                atualizarJogador(aux);
                            
                   }else if("all".equals(info[0])){
                       String bigstring = "";
                       for(int i=0;i<gerenciar.jogadores.size();i++){
                            bigstring += gerenciar.jogadores.get(i).getId()+";"+ gerenciar.jogadores.get(i).getNome()+";" +gerenciar.jogadores.get(i).getX()+";"+gerenciar.jogadores.get(i).getY()+";"+gerenciar.jogadores.get(i).getScore()+"|";
                        }
                       saida.println(bigstring);
                       System.out.println("ENVIOU -> " + bigstring);
                   }else if("id".equals(info[0])){
                       saida.println(pegarID());
                   }else if("allcoins".equals(info[0])){
                       //System.out.println("Cliente pediu moedas");
                       String bigstring = "";
                       //System.out.println("Tamanho de moedas" + gerenciarMoedas.moedas.size());
                       for(int t=0;t<gerenciarMoedas.moedas.size();t++){
                            bigstring += gerenciarMoedas.moedas.get(t).getId()+";"+gerenciarMoedas.moedas.get(t).getX()+";"+gerenciarMoedas.moedas.get(t).getY()+"|";
                        }
                       saida.println(bigstring);
                       System.out.println("ENVIOU MOEDAS -> " + bigstring);
                   }
                   else if("rm".equals(info[0])){
                       excluirMoeda(Integer.parseInt(info[1]));
                   }
                   else if("FIM".equals(info[0])){
                       excluirJogador(info[1]);
                   }
                   
            } while (!mensagem.equals("FIM"));

                saida.println(String.valueOf(contador - 1));

                System.out.println("Conexao encerrada pelo cliente");
                sair = true;
                saida.close();
                entrada.close();
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(ServidorTCP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }  // fim do metodo run

    }


}
