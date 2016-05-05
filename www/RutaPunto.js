/**
 * This class contains RutaPunto information.
 * @param {Object} IdPunto
 * @param {Object} IdRuta
 * @param {Object} IdUsuario
 * @param {Object} CoordX
 * @param {Object} CoordY
 * @param {Object} FechaHora
 * @param {Object} Heading
 * @param {Object} Velocidad
 * @param {Object} Altitud
 * @param {Object} Accuracy
 * @param {Object} FechaHoraCapturaRuta
 * @param {Object} Valido
 * @constructor
 */
var RutaPunto = function(id, id_ruta_local, id_usuario, coord_x, coord_y, fecha_hora, heading, velocidad, altitud, accuracy, fecha_hora_captura, valido) {
    this.IdPunto = id;
    this.IdRuta = id_ruta_local;
    this.IdUsuario = id_usuario;
    this.CoordX = coord_x;
    this.CoordY = coord_y;
    this.FechaHora = fecha_hora;
    this.Heading = heading;
    this.Velocidad = velocidad;
    this.Altitud = altitud;
    this.Accuracy = accuracy;
    this.FechaHoraCapturaRuta = fecha_hora_captura;
    this.Valido = valido;
};

module.exports = RutaPunto;