/**
 * This class contains Ruta information.
 * @param {Object} IdRuta
 * @param {Object} IdUsuario
 * @param {Object} FechaHoraInicio
 * @param {Object} FechaHoraFin
 * @param {Object} Duracion
 * @param {Object} Distancia
 * @param {Object} Observaciones
 * @param {Object} DireccionInicio
 * @param {Object} DireccionFin
 * @param {Object} EsIOS
 * @constructor
 */

var RutaPunto = require('./RutaPunto');

var Ruta = function(id, id_usuario, fecha_hora_inicio, fecha_hora_fin, duracion, distancia, observaciones, direccion_inicio, direccion_fin, lista_puntos) {
    this.IdRuta = id;
    this.IdUsuario = id_usuario;
    this.FechaHoraInicio = fecha_hora_inicio;
    this.FechaHoraFin = fecha_hora_fin;
    this.Duracion = duracion;
    this.Distancia = distancia;
    this.Observaciones = observaciones;
    this.DireccionInicio = direccion_inicio;
    this.DireccionFin = direccion_fin;
    this.EsIOS = 0;
    this.ListaPuntos = [];
    if (lista_puntos) {
        for(var i in lista_puntos) {
            var p = lista_puntos[i];
            var punto = new RutaPunto(p.IdPunto, p.IdRuta, p.IdUsuario, p.CoordX, p.CoordY, p.FechaHora, p.Heading, p.Velocidad, p.Altitud, p.Accuracy, p.FechaHoraCapturaRuta, p.Valido);
            this.ListaPuntos.push(punto);
        }
    }
};

module.exports = Ruta;