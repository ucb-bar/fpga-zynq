module MultiWidthFifo_0(input clk, input reset,
    output io_in_ready,
    input  io_in_valid,
    input [15:0] io_in_bits,
    input  io_out_ready,
    output io_out_valid,
    output[31:0] io_out_bits,
    output[5:0] io_count
);

  wire[5:0] T0;
  reg [6:0] R1;
  wire[6:0] T376;
  wire[6:0] T2;
  wire[6:0] T3;
  wire[6:0] T4;
  wire[6:0] T5;
  wire T6;
  wire[6:0] T7;
  wire T8;
  wire[6:0] T9;
  wire T10;
  wire T11;
  wire T12;
  wire[31:0] T13;
  wire[31:0] T14;
  wire[31:0] T15;
  wire[31:0] T16;
  wire[31:0] T17;
  wire[31:0] T18;
  reg [15:0] R19;
  wire[15:0] T20;
  wire T21;
  wire T22;
  wire[63:0] T23;
  wire[5:0] T24;
  reg [5:0] R25;
  wire[5:0] T377;
  wire[5:0] T26;
  wire[5:0] T27;
  wire T28;
  reg [15:0] R29;
  wire[15:0] T30;
  wire T31;
  wire T32;
  wire[31:0] T33;
  reg [15:0] R34;
  wire[15:0] T35;
  wire T36;
  wire T37;
  reg [15:0] R38;
  wire[15:0] T39;
  wire T40;
  wire T41;
  wire T42;
  wire[4:0] T43;
  reg [4:0] R44;
  wire[4:0] T378;
  wire[4:0] T45;
  wire[4:0] T46;
  wire T47;
  wire[31:0] T48;
  wire[31:0] T49;
  reg [15:0] R50;
  wire[15:0] T51;
  wire T52;
  wire T53;
  reg [15:0] R54;
  wire[15:0] T55;
  wire T56;
  wire T57;
  wire[31:0] T58;
  reg [15:0] R59;
  wire[15:0] T60;
  wire T61;
  wire T62;
  reg [15:0] R63;
  wire[15:0] T64;
  wire T65;
  wire T66;
  wire T67;
  wire T68;
  wire[31:0] T69;
  wire[31:0] T70;
  wire[31:0] T71;
  reg [15:0] R72;
  wire[15:0] T73;
  wire T74;
  wire T75;
  reg [15:0] R76;
  wire[15:0] T77;
  wire T78;
  wire T79;
  wire[31:0] T80;
  reg [15:0] R81;
  wire[15:0] T82;
  wire T83;
  wire T84;
  reg [15:0] R85;
  wire[15:0] T86;
  wire T87;
  wire T88;
  wire T89;
  wire[31:0] T90;
  wire[31:0] T91;
  reg [15:0] R92;
  wire[15:0] T93;
  wire T94;
  wire T95;
  reg [15:0] R96;
  wire[15:0] T97;
  wire T98;
  wire T99;
  wire[31:0] T100;
  reg [15:0] R101;
  wire[15:0] T102;
  wire T103;
  wire T104;
  reg [15:0] R105;
  wire[15:0] T106;
  wire T107;
  wire T108;
  wire T109;
  wire T110;
  wire T111;
  wire[31:0] T112;
  wire[31:0] T113;
  wire[31:0] T114;
  wire[31:0] T115;
  reg [15:0] R116;
  wire[15:0] T117;
  wire T118;
  wire T119;
  reg [15:0] R120;
  wire[15:0] T121;
  wire T122;
  wire T123;
  wire[31:0] T124;
  reg [15:0] R125;
  wire[15:0] T126;
  wire T127;
  wire T128;
  reg [15:0] R129;
  wire[15:0] T130;
  wire T131;
  wire T132;
  wire T133;
  wire[31:0] T134;
  wire[31:0] T135;
  reg [15:0] R136;
  wire[15:0] T137;
  wire T138;
  wire T139;
  reg [15:0] R140;
  wire[15:0] T141;
  wire T142;
  wire T143;
  wire[31:0] T144;
  reg [15:0] R145;
  wire[15:0] T146;
  wire T147;
  wire T148;
  reg [15:0] R149;
  wire[15:0] T150;
  wire T151;
  wire T152;
  wire T153;
  wire T154;
  wire[31:0] T155;
  wire[31:0] T156;
  wire[31:0] T157;
  reg [15:0] R158;
  wire[15:0] T159;
  wire T160;
  wire T161;
  reg [15:0] R162;
  wire[15:0] T163;
  wire T164;
  wire T165;
  wire[31:0] T166;
  reg [15:0] R167;
  wire[15:0] T168;
  wire T169;
  wire T170;
  reg [15:0] R171;
  wire[15:0] T172;
  wire T173;
  wire T174;
  wire T175;
  wire[31:0] T176;
  wire[31:0] T177;
  reg [15:0] R178;
  wire[15:0] T179;
  wire T180;
  wire T181;
  reg [15:0] R182;
  wire[15:0] T183;
  wire T184;
  wire T185;
  wire[31:0] T186;
  reg [15:0] R187;
  wire[15:0] T188;
  wire T189;
  wire T190;
  reg [15:0] R191;
  wire[15:0] T192;
  wire T193;
  wire T194;
  wire T195;
  wire T196;
  wire T197;
  wire T198;
  wire[31:0] T199;
  wire[31:0] T200;
  wire[31:0] T201;
  wire[31:0] T202;
  wire[31:0] T203;
  reg [15:0] R204;
  wire[15:0] T205;
  wire T206;
  wire T207;
  reg [15:0] R208;
  wire[15:0] T209;
  wire T210;
  wire T211;
  wire[31:0] T212;
  reg [15:0] R213;
  wire[15:0] T214;
  wire T215;
  wire T216;
  reg [15:0] R217;
  wire[15:0] T218;
  wire T219;
  wire T220;
  wire T221;
  wire[31:0] T222;
  wire[31:0] T223;
  reg [15:0] R224;
  wire[15:0] T225;
  wire T226;
  wire T227;
  reg [15:0] R228;
  wire[15:0] T229;
  wire T230;
  wire T231;
  wire[31:0] T232;
  reg [15:0] R233;
  wire[15:0] T234;
  wire T235;
  wire T236;
  reg [15:0] R237;
  wire[15:0] T238;
  wire T239;
  wire T240;
  wire T241;
  wire T242;
  wire[31:0] T243;
  wire[31:0] T244;
  wire[31:0] T245;
  reg [15:0] R246;
  wire[15:0] T247;
  wire T248;
  wire T249;
  reg [15:0] R250;
  wire[15:0] T251;
  wire T252;
  wire T253;
  wire[31:0] T254;
  reg [15:0] R255;
  wire[15:0] T256;
  wire T257;
  wire T258;
  reg [15:0] R259;
  wire[15:0] T260;
  wire T261;
  wire T262;
  wire T263;
  wire[31:0] T264;
  wire[31:0] T265;
  reg [15:0] R266;
  wire[15:0] T267;
  wire T268;
  wire T269;
  reg [15:0] R270;
  wire[15:0] T271;
  wire T272;
  wire T273;
  wire[31:0] T274;
  reg [15:0] R275;
  wire[15:0] T276;
  wire T277;
  wire T278;
  reg [15:0] R279;
  wire[15:0] T280;
  wire T281;
  wire T282;
  wire T283;
  wire T284;
  wire T285;
  wire[31:0] T286;
  wire[31:0] T287;
  wire[31:0] T288;
  wire[31:0] T289;
  reg [15:0] R290;
  wire[15:0] T291;
  wire T292;
  wire T293;
  reg [15:0] R294;
  wire[15:0] T295;
  wire T296;
  wire T297;
  wire[31:0] T298;
  reg [15:0] R299;
  wire[15:0] T300;
  wire T301;
  wire T302;
  reg [15:0] R303;
  wire[15:0] T304;
  wire T305;
  wire T306;
  wire T307;
  wire[31:0] T308;
  wire[31:0] T309;
  reg [15:0] R310;
  wire[15:0] T311;
  wire T312;
  wire T313;
  reg [15:0] R314;
  wire[15:0] T315;
  wire T316;
  wire T317;
  wire[31:0] T318;
  reg [15:0] R319;
  wire[15:0] T320;
  wire T321;
  wire T322;
  reg [15:0] R323;
  wire[15:0] T324;
  wire T325;
  wire T326;
  wire T327;
  wire T328;
  wire[31:0] T329;
  wire[31:0] T330;
  wire[31:0] T331;
  reg [15:0] R332;
  wire[15:0] T333;
  wire T334;
  wire T335;
  reg [15:0] R336;
  wire[15:0] T337;
  wire T338;
  wire T339;
  wire[31:0] T340;
  reg [15:0] R341;
  wire[15:0] T342;
  wire T343;
  wire T344;
  reg [15:0] R345;
  wire[15:0] T346;
  wire T347;
  wire T348;
  wire T349;
  wire[31:0] T350;
  wire[31:0] T351;
  reg [15:0] R352;
  wire[15:0] T353;
  wire T354;
  wire T355;
  reg [15:0] R356;
  wire[15:0] T357;
  wire T358;
  wire T359;
  wire[31:0] T360;
  reg [15:0] R361;
  wire[15:0] T362;
  wire T363;
  wire T364;
  reg [15:0] R365;
  wire[15:0] T366;
  wire T367;
  wire T368;
  wire T369;
  wire T370;
  wire T371;
  wire T372;
  wire T373;
  wire T374;
  wire T375;

`ifndef SYNTHESIS
// synthesis translate_off
  integer initvar;
  initial begin
    #0.002;
    R1 = {1{$random}};
    R19 = {1{$random}};
    R25 = {1{$random}};
    R29 = {1{$random}};
    R34 = {1{$random}};
    R38 = {1{$random}};
    R44 = {1{$random}};
    R50 = {1{$random}};
    R54 = {1{$random}};
    R59 = {1{$random}};
    R63 = {1{$random}};
    R72 = {1{$random}};
    R76 = {1{$random}};
    R81 = {1{$random}};
    R85 = {1{$random}};
    R92 = {1{$random}};
    R96 = {1{$random}};
    R101 = {1{$random}};
    R105 = {1{$random}};
    R116 = {1{$random}};
    R120 = {1{$random}};
    R125 = {1{$random}};
    R129 = {1{$random}};
    R136 = {1{$random}};
    R140 = {1{$random}};
    R145 = {1{$random}};
    R149 = {1{$random}};
    R158 = {1{$random}};
    R162 = {1{$random}};
    R167 = {1{$random}};
    R171 = {1{$random}};
    R178 = {1{$random}};
    R182 = {1{$random}};
    R187 = {1{$random}};
    R191 = {1{$random}};
    R204 = {1{$random}};
    R208 = {1{$random}};
    R213 = {1{$random}};
    R217 = {1{$random}};
    R224 = {1{$random}};
    R228 = {1{$random}};
    R233 = {1{$random}};
    R237 = {1{$random}};
    R246 = {1{$random}};
    R250 = {1{$random}};
    R255 = {1{$random}};
    R259 = {1{$random}};
    R266 = {1{$random}};
    R270 = {1{$random}};
    R275 = {1{$random}};
    R279 = {1{$random}};
    R290 = {1{$random}};
    R294 = {1{$random}};
    R299 = {1{$random}};
    R303 = {1{$random}};
    R310 = {1{$random}};
    R314 = {1{$random}};
    R319 = {1{$random}};
    R323 = {1{$random}};
    R332 = {1{$random}};
    R336 = {1{$random}};
    R341 = {1{$random}};
    R345 = {1{$random}};
    R352 = {1{$random}};
    R356 = {1{$random}};
    R361 = {1{$random}};
    R365 = {1{$random}};
  end
// synthesis translate_on
`endif

  assign io_count = T0;
  assign T0 = R1 >> 1'h1;
  assign T376 = reset ? 7'h0 : T2;
  assign T2 = T10 ? T9 : T3;
  assign T3 = T8 ? T7 : T4;
  assign T4 = T6 ? T5 : R1;
  assign T5 = R1 - 7'h2;
  assign T6 = io_out_ready & io_out_valid;
  assign T7 = R1 + 7'h1;
  assign T8 = io_in_ready & io_in_valid;
  assign T9 = R1 - 7'h1;
  assign T10 = T12 & T11;
  assign T11 = io_out_ready & io_out_valid;
  assign T12 = io_in_ready & io_in_valid;
  assign io_out_bits = T13;
  assign T13 = T373 ? T199 : T14;
  assign T14 = T198 ? T112 : T15;
  assign T15 = T111 ? T69 : T16;
  assign T16 = T68 ? T48 : T17;
  assign T17 = T42 ? T33 : T18;
  assign T18 = {R29, R19};
  assign T20 = T21 ? io_in_bits : R19;
  assign T21 = T28 & T22;
  assign T22 = T23[1'h0:1'h0];
  assign T23 = 1'h1 << T24;
  assign T24 = R25;
  assign T377 = reset ? 6'h0 : T26;
  assign T26 = T28 ? T27 : R25;
  assign T27 = R25 + 6'h1;
  assign T28 = io_in_ready & io_in_valid;
  assign T30 = T31 ? io_in_bits : R29;
  assign T31 = T28 & T32;
  assign T32 = T23[1'h1:1'h1];
  assign T33 = {R38, R34};
  assign T35 = T36 ? io_in_bits : R34;
  assign T36 = T28 & T37;
  assign T37 = T23[2'h2:2'h2];
  assign T39 = T40 ? io_in_bits : R38;
  assign T40 = T28 & T41;
  assign T41 = T23[2'h3:2'h3];
  assign T42 = T43[1'h0:1'h0];
  assign T43 = R44;
  assign T378 = reset ? 5'h0 : T45;
  assign T45 = T47 ? T46 : R44;
  assign T46 = R44 + 5'h1;
  assign T47 = io_out_ready & io_out_valid;
  assign T48 = T67 ? T58 : T49;
  assign T49 = {R54, R50};
  assign T51 = T52 ? io_in_bits : R50;
  assign T52 = T28 & T53;
  assign T53 = T23[3'h4:3'h4];
  assign T55 = T56 ? io_in_bits : R54;
  assign T56 = T28 & T57;
  assign T57 = T23[3'h5:3'h5];
  assign T58 = {R63, R59};
  assign T60 = T61 ? io_in_bits : R59;
  assign T61 = T28 & T62;
  assign T62 = T23[3'h6:3'h6];
  assign T64 = T65 ? io_in_bits : R63;
  assign T65 = T28 & T66;
  assign T66 = T23[3'h7:3'h7];
  assign T67 = T43[1'h0:1'h0];
  assign T68 = T43[1'h1:1'h1];
  assign T69 = T110 ? T90 : T70;
  assign T70 = T89 ? T80 : T71;
  assign T71 = {R76, R72};
  assign T73 = T74 ? io_in_bits : R72;
  assign T74 = T28 & T75;
  assign T75 = T23[4'h8:4'h8];
  assign T77 = T78 ? io_in_bits : R76;
  assign T78 = T28 & T79;
  assign T79 = T23[4'h9:4'h9];
  assign T80 = {R85, R81};
  assign T82 = T83 ? io_in_bits : R81;
  assign T83 = T28 & T84;
  assign T84 = T23[4'ha:4'ha];
  assign T86 = T87 ? io_in_bits : R85;
  assign T87 = T28 & T88;
  assign T88 = T23[4'hb:4'hb];
  assign T89 = T43[1'h0:1'h0];
  assign T90 = T109 ? T100 : T91;
  assign T91 = {R96, R92};
  assign T93 = T94 ? io_in_bits : R92;
  assign T94 = T28 & T95;
  assign T95 = T23[4'hc:4'hc];
  assign T97 = T98 ? io_in_bits : R96;
  assign T98 = T28 & T99;
  assign T99 = T23[4'hd:4'hd];
  assign T100 = {R105, R101};
  assign T102 = T103 ? io_in_bits : R101;
  assign T103 = T28 & T104;
  assign T104 = T23[4'he:4'he];
  assign T106 = T107 ? io_in_bits : R105;
  assign T107 = T28 & T108;
  assign T108 = T23[4'hf:4'hf];
  assign T109 = T43[1'h0:1'h0];
  assign T110 = T43[1'h1:1'h1];
  assign T111 = T43[2'h2:2'h2];
  assign T112 = T197 ? T155 : T113;
  assign T113 = T154 ? T134 : T114;
  assign T114 = T133 ? T124 : T115;
  assign T115 = {R120, R116};
  assign T117 = T118 ? io_in_bits : R116;
  assign T118 = T28 & T119;
  assign T119 = T23[5'h10:5'h10];
  assign T121 = T122 ? io_in_bits : R120;
  assign T122 = T28 & T123;
  assign T123 = T23[5'h11:5'h11];
  assign T124 = {R129, R125};
  assign T126 = T127 ? io_in_bits : R125;
  assign T127 = T28 & T128;
  assign T128 = T23[5'h12:5'h12];
  assign T130 = T131 ? io_in_bits : R129;
  assign T131 = T28 & T132;
  assign T132 = T23[5'h13:5'h13];
  assign T133 = T43[1'h0:1'h0];
  assign T134 = T153 ? T144 : T135;
  assign T135 = {R140, R136};
  assign T137 = T138 ? io_in_bits : R136;
  assign T138 = T28 & T139;
  assign T139 = T23[5'h14:5'h14];
  assign T141 = T142 ? io_in_bits : R140;
  assign T142 = T28 & T143;
  assign T143 = T23[5'h15:5'h15];
  assign T144 = {R149, R145};
  assign T146 = T147 ? io_in_bits : R145;
  assign T147 = T28 & T148;
  assign T148 = T23[5'h16:5'h16];
  assign T150 = T151 ? io_in_bits : R149;
  assign T151 = T28 & T152;
  assign T152 = T23[5'h17:5'h17];
  assign T153 = T43[1'h0:1'h0];
  assign T154 = T43[1'h1:1'h1];
  assign T155 = T196 ? T176 : T156;
  assign T156 = T175 ? T166 : T157;
  assign T157 = {R162, R158};
  assign T159 = T160 ? io_in_bits : R158;
  assign T160 = T28 & T161;
  assign T161 = T23[5'h18:5'h18];
  assign T163 = T164 ? io_in_bits : R162;
  assign T164 = T28 & T165;
  assign T165 = T23[5'h19:5'h19];
  assign T166 = {R171, R167};
  assign T168 = T169 ? io_in_bits : R167;
  assign T169 = T28 & T170;
  assign T170 = T23[5'h1a:5'h1a];
  assign T172 = T173 ? io_in_bits : R171;
  assign T173 = T28 & T174;
  assign T174 = T23[5'h1b:5'h1b];
  assign T175 = T43[1'h0:1'h0];
  assign T176 = T195 ? T186 : T177;
  assign T177 = {R182, R178};
  assign T179 = T180 ? io_in_bits : R178;
  assign T180 = T28 & T181;
  assign T181 = T23[5'h1c:5'h1c];
  assign T183 = T184 ? io_in_bits : R182;
  assign T184 = T28 & T185;
  assign T185 = T23[5'h1d:5'h1d];
  assign T186 = {R191, R187};
  assign T188 = T189 ? io_in_bits : R187;
  assign T189 = T28 & T190;
  assign T190 = T23[5'h1e:5'h1e];
  assign T192 = T193 ? io_in_bits : R191;
  assign T193 = T28 & T194;
  assign T194 = T23[5'h1f:5'h1f];
  assign T195 = T43[1'h0:1'h0];
  assign T196 = T43[1'h1:1'h1];
  assign T197 = T43[2'h2:2'h2];
  assign T198 = T43[2'h3:2'h3];
  assign T199 = T372 ? T286 : T200;
  assign T200 = T285 ? T243 : T201;
  assign T201 = T242 ? T222 : T202;
  assign T202 = T221 ? T212 : T203;
  assign T203 = {R208, R204};
  assign T205 = T206 ? io_in_bits : R204;
  assign T206 = T28 & T207;
  assign T207 = T23[6'h20:6'h20];
  assign T209 = T210 ? io_in_bits : R208;
  assign T210 = T28 & T211;
  assign T211 = T23[6'h21:6'h21];
  assign T212 = {R217, R213};
  assign T214 = T215 ? io_in_bits : R213;
  assign T215 = T28 & T216;
  assign T216 = T23[6'h22:6'h22];
  assign T218 = T219 ? io_in_bits : R217;
  assign T219 = T28 & T220;
  assign T220 = T23[6'h23:6'h23];
  assign T221 = T43[1'h0:1'h0];
  assign T222 = T241 ? T232 : T223;
  assign T223 = {R228, R224};
  assign T225 = T226 ? io_in_bits : R224;
  assign T226 = T28 & T227;
  assign T227 = T23[6'h24:6'h24];
  assign T229 = T230 ? io_in_bits : R228;
  assign T230 = T28 & T231;
  assign T231 = T23[6'h25:6'h25];
  assign T232 = {R237, R233};
  assign T234 = T235 ? io_in_bits : R233;
  assign T235 = T28 & T236;
  assign T236 = T23[6'h26:6'h26];
  assign T238 = T239 ? io_in_bits : R237;
  assign T239 = T28 & T240;
  assign T240 = T23[6'h27:6'h27];
  assign T241 = T43[1'h0:1'h0];
  assign T242 = T43[1'h1:1'h1];
  assign T243 = T284 ? T264 : T244;
  assign T244 = T263 ? T254 : T245;
  assign T245 = {R250, R246};
  assign T247 = T248 ? io_in_bits : R246;
  assign T248 = T28 & T249;
  assign T249 = T23[6'h28:6'h28];
  assign T251 = T252 ? io_in_bits : R250;
  assign T252 = T28 & T253;
  assign T253 = T23[6'h29:6'h29];
  assign T254 = {R259, R255};
  assign T256 = T257 ? io_in_bits : R255;
  assign T257 = T28 & T258;
  assign T258 = T23[6'h2a:6'h2a];
  assign T260 = T261 ? io_in_bits : R259;
  assign T261 = T28 & T262;
  assign T262 = T23[6'h2b:6'h2b];
  assign T263 = T43[1'h0:1'h0];
  assign T264 = T283 ? T274 : T265;
  assign T265 = {R270, R266};
  assign T267 = T268 ? io_in_bits : R266;
  assign T268 = T28 & T269;
  assign T269 = T23[6'h2c:6'h2c];
  assign T271 = T272 ? io_in_bits : R270;
  assign T272 = T28 & T273;
  assign T273 = T23[6'h2d:6'h2d];
  assign T274 = {R279, R275};
  assign T276 = T277 ? io_in_bits : R275;
  assign T277 = T28 & T278;
  assign T278 = T23[6'h2e:6'h2e];
  assign T280 = T281 ? io_in_bits : R279;
  assign T281 = T28 & T282;
  assign T282 = T23[6'h2f:6'h2f];
  assign T283 = T43[1'h0:1'h0];
  assign T284 = T43[1'h1:1'h1];
  assign T285 = T43[2'h2:2'h2];
  assign T286 = T371 ? T329 : T287;
  assign T287 = T328 ? T308 : T288;
  assign T288 = T307 ? T298 : T289;
  assign T289 = {R294, R290};
  assign T291 = T292 ? io_in_bits : R290;
  assign T292 = T28 & T293;
  assign T293 = T23[6'h30:6'h30];
  assign T295 = T296 ? io_in_bits : R294;
  assign T296 = T28 & T297;
  assign T297 = T23[6'h31:6'h31];
  assign T298 = {R303, R299};
  assign T300 = T301 ? io_in_bits : R299;
  assign T301 = T28 & T302;
  assign T302 = T23[6'h32:6'h32];
  assign T304 = T305 ? io_in_bits : R303;
  assign T305 = T28 & T306;
  assign T306 = T23[6'h33:6'h33];
  assign T307 = T43[1'h0:1'h0];
  assign T308 = T327 ? T318 : T309;
  assign T309 = {R314, R310};
  assign T311 = T312 ? io_in_bits : R310;
  assign T312 = T28 & T313;
  assign T313 = T23[6'h34:6'h34];
  assign T315 = T316 ? io_in_bits : R314;
  assign T316 = T28 & T317;
  assign T317 = T23[6'h35:6'h35];
  assign T318 = {R323, R319};
  assign T320 = T321 ? io_in_bits : R319;
  assign T321 = T28 & T322;
  assign T322 = T23[6'h36:6'h36];
  assign T324 = T325 ? io_in_bits : R323;
  assign T325 = T28 & T326;
  assign T326 = T23[6'h37:6'h37];
  assign T327 = T43[1'h0:1'h0];
  assign T328 = T43[1'h1:1'h1];
  assign T329 = T370 ? T350 : T330;
  assign T330 = T349 ? T340 : T331;
  assign T331 = {R336, R332};
  assign T333 = T334 ? io_in_bits : R332;
  assign T334 = T28 & T335;
  assign T335 = T23[6'h38:6'h38];
  assign T337 = T338 ? io_in_bits : R336;
  assign T338 = T28 & T339;
  assign T339 = T23[6'h39:6'h39];
  assign T340 = {R345, R341};
  assign T342 = T343 ? io_in_bits : R341;
  assign T343 = T28 & T344;
  assign T344 = T23[6'h3a:6'h3a];
  assign T346 = T347 ? io_in_bits : R345;
  assign T347 = T28 & T348;
  assign T348 = T23[6'h3b:6'h3b];
  assign T349 = T43[1'h0:1'h0];
  assign T350 = T369 ? T360 : T351;
  assign T351 = {R356, R352};
  assign T353 = T354 ? io_in_bits : R352;
  assign T354 = T28 & T355;
  assign T355 = T23[6'h3c:6'h3c];
  assign T357 = T358 ? io_in_bits : R356;
  assign T358 = T28 & T359;
  assign T359 = T23[6'h3d:6'h3d];
  assign T360 = {R365, R361};
  assign T362 = T363 ? io_in_bits : R361;
  assign T363 = T28 & T364;
  assign T364 = T23[6'h3e:6'h3e];
  assign T366 = T367 ? io_in_bits : R365;
  assign T367 = T28 & T368;
  assign T368 = T23[6'h3f:6'h3f];
  assign T369 = T43[1'h0:1'h0];
  assign T370 = T43[1'h1:1'h1];
  assign T371 = T43[2'h2:2'h2];
  assign T372 = T43[2'h3:2'h3];
  assign T373 = T43[3'h4:3'h4];
  assign io_out_valid = T374;
  assign T374 = 6'h0 < io_count;
  assign io_in_ready = T375;
  assign T375 = R1 < 7'h40;

  always @(posedge clk) begin
    if(reset) begin
      R1 <= 7'h0;
    end else if(T10) begin
      R1 <= T9;
    end else if(T8) begin
      R1 <= T7;
    end else if(T6) begin
      R1 <= T5;
    end
    if(T21) begin
      R19 <= io_in_bits;
    end
    if(reset) begin
      R25 <= 6'h0;
    end else if(T28) begin
      R25 <= T27;
    end
    if(T31) begin
      R29 <= io_in_bits;
    end
    if(T36) begin
      R34 <= io_in_bits;
    end
    if(T40) begin
      R38 <= io_in_bits;
    end
    if(reset) begin
      R44 <= 5'h0;
    end else if(T47) begin
      R44 <= T46;
    end
    if(T52) begin
      R50 <= io_in_bits;
    end
    if(T56) begin
      R54 <= io_in_bits;
    end
    if(T61) begin
      R59 <= io_in_bits;
    end
    if(T65) begin
      R63 <= io_in_bits;
    end
    if(T74) begin
      R72 <= io_in_bits;
    end
    if(T78) begin
      R76 <= io_in_bits;
    end
    if(T83) begin
      R81 <= io_in_bits;
    end
    if(T87) begin
      R85 <= io_in_bits;
    end
    if(T94) begin
      R92 <= io_in_bits;
    end
    if(T98) begin
      R96 <= io_in_bits;
    end
    if(T103) begin
      R101 <= io_in_bits;
    end
    if(T107) begin
      R105 <= io_in_bits;
    end
    if(T118) begin
      R116 <= io_in_bits;
    end
    if(T122) begin
      R120 <= io_in_bits;
    end
    if(T127) begin
      R125 <= io_in_bits;
    end
    if(T131) begin
      R129 <= io_in_bits;
    end
    if(T138) begin
      R136 <= io_in_bits;
    end
    if(T142) begin
      R140 <= io_in_bits;
    end
    if(T147) begin
      R145 <= io_in_bits;
    end
    if(T151) begin
      R149 <= io_in_bits;
    end
    if(T160) begin
      R158 <= io_in_bits;
    end
    if(T164) begin
      R162 <= io_in_bits;
    end
    if(T169) begin
      R167 <= io_in_bits;
    end
    if(T173) begin
      R171 <= io_in_bits;
    end
    if(T180) begin
      R178 <= io_in_bits;
    end
    if(T184) begin
      R182 <= io_in_bits;
    end
    if(T189) begin
      R187 <= io_in_bits;
    end
    if(T193) begin
      R191 <= io_in_bits;
    end
    if(T206) begin
      R204 <= io_in_bits;
    end
    if(T210) begin
      R208 <= io_in_bits;
    end
    if(T215) begin
      R213 <= io_in_bits;
    end
    if(T219) begin
      R217 <= io_in_bits;
    end
    if(T226) begin
      R224 <= io_in_bits;
    end
    if(T230) begin
      R228 <= io_in_bits;
    end
    if(T235) begin
      R233 <= io_in_bits;
    end
    if(T239) begin
      R237 <= io_in_bits;
    end
    if(T248) begin
      R246 <= io_in_bits;
    end
    if(T252) begin
      R250 <= io_in_bits;
    end
    if(T257) begin
      R255 <= io_in_bits;
    end
    if(T261) begin
      R259 <= io_in_bits;
    end
    if(T268) begin
      R266 <= io_in_bits;
    end
    if(T272) begin
      R270 <= io_in_bits;
    end
    if(T277) begin
      R275 <= io_in_bits;
    end
    if(T281) begin
      R279 <= io_in_bits;
    end
    if(T292) begin
      R290 <= io_in_bits;
    end
    if(T296) begin
      R294 <= io_in_bits;
    end
    if(T301) begin
      R299 <= io_in_bits;
    end
    if(T305) begin
      R303 <= io_in_bits;
    end
    if(T312) begin
      R310 <= io_in_bits;
    end
    if(T316) begin
      R314 <= io_in_bits;
    end
    if(T321) begin
      R319 <= io_in_bits;
    end
    if(T325) begin
      R323 <= io_in_bits;
    end
    if(T334) begin
      R332 <= io_in_bits;
    end
    if(T338) begin
      R336 <= io_in_bits;
    end
    if(T343) begin
      R341 <= io_in_bits;
    end
    if(T347) begin
      R345 <= io_in_bits;
    end
    if(T354) begin
      R352 <= io_in_bits;
    end
    if(T358) begin
      R356 <= io_in_bits;
    end
    if(T363) begin
      R361 <= io_in_bits;
    end
    if(T367) begin
      R365 <= io_in_bits;
    end
  end
endmodule

module MultiWidthFifo_1(input clk, input reset,
    output io_in_ready,
    input  io_in_valid,
    input [31:0] io_in_bits,
    input  io_out_ready,
    output io_out_valid,
    output[15:0] io_out_bits,
    output[5:0] io_count
);

  reg [5:0] R0;
  wire[5:0] T183;
  wire[5:0] T1;
  wire[5:0] T2;
  wire[5:0] T3;
  wire[5:0] T4;
  wire T5;
  wire[5:0] T6;
  wire T7;
  wire[5:0] T8;
  wire T9;
  wire T10;
  wire T11;
  wire[15:0] T12;
  wire[15:0] T13;
  wire[15:0] T14;
  wire[15:0] T15;
  wire[15:0] T16;
  wire[15:0] T17;
  reg [31:0] R18;
  wire[31:0] T19;
  wire T20;
  wire T21;
  wire[15:0] T22;
  wire[3:0] T23;
  reg [3:0] R24;
  wire[3:0] T184;
  wire[3:0] T25;
  wire[3:0] T26;
  wire T27;
  wire[15:0] T28;
  wire T29;
  wire[4:0] T30;
  reg [4:0] R31;
  wire[4:0] T185;
  wire[4:0] T32;
  wire[4:0] T33;
  wire T34;
  wire[15:0] T35;
  wire[15:0] T36;
  reg [31:0] R37;
  wire[31:0] T38;
  wire T39;
  wire T40;
  wire[15:0] T41;
  wire T42;
  wire T43;
  wire[15:0] T44;
  wire[15:0] T45;
  wire[15:0] T46;
  reg [31:0] R47;
  wire[31:0] T48;
  wire T49;
  wire T50;
  wire[15:0] T51;
  wire T52;
  wire[15:0] T53;
  wire[15:0] T54;
  reg [31:0] R55;
  wire[31:0] T56;
  wire T57;
  wire T58;
  wire[15:0] T59;
  wire T60;
  wire T61;
  wire T62;
  wire[15:0] T63;
  wire[15:0] T64;
  wire[15:0] T65;
  wire[15:0] T66;
  reg [31:0] R67;
  wire[31:0] T68;
  wire T69;
  wire T70;
  wire[15:0] T71;
  wire T72;
  wire[15:0] T73;
  wire[15:0] T74;
  reg [31:0] R75;
  wire[31:0] T76;
  wire T77;
  wire T78;
  wire[15:0] T79;
  wire T80;
  wire T81;
  wire[15:0] T82;
  wire[15:0] T83;
  wire[15:0] T84;
  reg [31:0] R85;
  wire[31:0] T86;
  wire T87;
  wire T88;
  wire[15:0] T89;
  wire T90;
  wire[15:0] T91;
  wire[15:0] T92;
  reg [31:0] R93;
  wire[31:0] T94;
  wire T95;
  wire T96;
  wire[15:0] T97;
  wire T98;
  wire T99;
  wire T100;
  wire T101;
  wire[15:0] T102;
  wire[15:0] T103;
  wire[15:0] T104;
  wire[15:0] T105;
  wire[15:0] T106;
  reg [31:0] R107;
  wire[31:0] T108;
  wire T109;
  wire T110;
  wire[15:0] T111;
  wire T112;
  wire[15:0] T113;
  wire[15:0] T114;
  reg [31:0] R115;
  wire[31:0] T116;
  wire T117;
  wire T118;
  wire[15:0] T119;
  wire T120;
  wire T121;
  wire[15:0] T122;
  wire[15:0] T123;
  wire[15:0] T124;
  reg [31:0] R125;
  wire[31:0] T126;
  wire T127;
  wire T128;
  wire[15:0] T129;
  wire T130;
  wire[15:0] T131;
  wire[15:0] T132;
  reg [31:0] R133;
  wire[31:0] T134;
  wire T135;
  wire T136;
  wire[15:0] T137;
  wire T138;
  wire T139;
  wire T140;
  wire[15:0] T141;
  wire[15:0] T142;
  wire[15:0] T143;
  wire[15:0] T144;
  reg [31:0] R145;
  wire[31:0] T146;
  wire T147;
  wire T148;
  wire[15:0] T149;
  wire T150;
  wire[15:0] T151;
  wire[15:0] T152;
  reg [31:0] R153;
  wire[31:0] T154;
  wire T155;
  wire T156;
  wire[15:0] T157;
  wire T158;
  wire T159;
  wire[15:0] T160;
  wire[15:0] T161;
  wire[15:0] T162;
  reg [31:0] R163;
  wire[31:0] T164;
  wire T165;
  wire T166;
  wire[15:0] T167;
  wire T168;
  wire[15:0] T169;
  wire[15:0] T170;
  reg [31:0] R171;
  wire[31:0] T172;
  wire T173;
  wire T174;
  wire[15:0] T175;
  wire T176;
  wire T177;
  wire T178;
  wire T179;
  wire T180;
  wire T181;
  wire T182;

`ifndef SYNTHESIS
// synthesis translate_off
  integer initvar;
  initial begin
    #0.002;
    R0 = {1{$random}};
    R18 = {1{$random}};
    R24 = {1{$random}};
    R31 = {1{$random}};
    R37 = {1{$random}};
    R47 = {1{$random}};
    R55 = {1{$random}};
    R67 = {1{$random}};
    R75 = {1{$random}};
    R85 = {1{$random}};
    R93 = {1{$random}};
    R107 = {1{$random}};
    R115 = {1{$random}};
    R125 = {1{$random}};
    R133 = {1{$random}};
    R145 = {1{$random}};
    R153 = {1{$random}};
    R163 = {1{$random}};
    R171 = {1{$random}};
  end
// synthesis translate_on
`endif

  assign io_count = R0;
  assign T183 = reset ? 6'h0 : T1;
  assign T1 = T9 ? T8 : T2;
  assign T2 = T7 ? T6 : T3;
  assign T3 = T5 ? T4 : R0;
  assign T4 = R0 - 6'h1;
  assign T5 = io_out_ready & io_out_valid;
  assign T6 = R0 + 6'h2;
  assign T7 = io_in_ready & io_in_valid;
  assign T8 = R0 + 6'h1;
  assign T9 = T11 & T10;
  assign T10 = io_out_ready & io_out_valid;
  assign T11 = io_in_ready & io_in_valid;
  assign io_out_bits = T12;
  assign T12 = T180 ? T102 : T13;
  assign T13 = T101 ? T63 : T14;
  assign T14 = T62 ? T44 : T15;
  assign T15 = T43 ? T35 : T16;
  assign T16 = T29 ? T28 : T17;
  assign T17 = R18[4'hf:1'h0];
  assign T19 = T20 ? io_in_bits : R18;
  assign T20 = T27 & T21;
  assign T21 = T22[1'h0:1'h0];
  assign T22 = 1'h1 << T23;
  assign T23 = R24;
  assign T184 = reset ? 4'h0 : T25;
  assign T25 = T27 ? T26 : R24;
  assign T26 = R24 + 4'h1;
  assign T27 = io_in_ready & io_in_valid;
  assign T28 = R18[5'h1f:5'h10];
  assign T29 = T30[1'h0:1'h0];
  assign T30 = R31;
  assign T185 = reset ? 5'h0 : T32;
  assign T32 = T34 ? T33 : R31;
  assign T33 = R31 + 5'h1;
  assign T34 = io_out_ready & io_out_valid;
  assign T35 = T42 ? T41 : T36;
  assign T36 = R37[4'hf:1'h0];
  assign T38 = T39 ? io_in_bits : R37;
  assign T39 = T27 & T40;
  assign T40 = T22[1'h1:1'h1];
  assign T41 = R37[5'h1f:5'h10];
  assign T42 = T30[1'h0:1'h0];
  assign T43 = T30[1'h1:1'h1];
  assign T44 = T61 ? T53 : T45;
  assign T45 = T52 ? T51 : T46;
  assign T46 = R47[4'hf:1'h0];
  assign T48 = T49 ? io_in_bits : R47;
  assign T49 = T27 & T50;
  assign T50 = T22[2'h2:2'h2];
  assign T51 = R47[5'h1f:5'h10];
  assign T52 = T30[1'h0:1'h0];
  assign T53 = T60 ? T59 : T54;
  assign T54 = R55[4'hf:1'h0];
  assign T56 = T57 ? io_in_bits : R55;
  assign T57 = T27 & T58;
  assign T58 = T22[2'h3:2'h3];
  assign T59 = R55[5'h1f:5'h10];
  assign T60 = T30[1'h0:1'h0];
  assign T61 = T30[1'h1:1'h1];
  assign T62 = T30[2'h2:2'h2];
  assign T63 = T100 ? T82 : T64;
  assign T64 = T81 ? T73 : T65;
  assign T65 = T72 ? T71 : T66;
  assign T66 = R67[4'hf:1'h0];
  assign T68 = T69 ? io_in_bits : R67;
  assign T69 = T27 & T70;
  assign T70 = T22[3'h4:3'h4];
  assign T71 = R67[5'h1f:5'h10];
  assign T72 = T30[1'h0:1'h0];
  assign T73 = T80 ? T79 : T74;
  assign T74 = R75[4'hf:1'h0];
  assign T76 = T77 ? io_in_bits : R75;
  assign T77 = T27 & T78;
  assign T78 = T22[3'h5:3'h5];
  assign T79 = R75[5'h1f:5'h10];
  assign T80 = T30[1'h0:1'h0];
  assign T81 = T30[1'h1:1'h1];
  assign T82 = T99 ? T91 : T83;
  assign T83 = T90 ? T89 : T84;
  assign T84 = R85[4'hf:1'h0];
  assign T86 = T87 ? io_in_bits : R85;
  assign T87 = T27 & T88;
  assign T88 = T22[3'h6:3'h6];
  assign T89 = R85[5'h1f:5'h10];
  assign T90 = T30[1'h0:1'h0];
  assign T91 = T98 ? T97 : T92;
  assign T92 = R93[4'hf:1'h0];
  assign T94 = T95 ? io_in_bits : R93;
  assign T95 = T27 & T96;
  assign T96 = T22[3'h7:3'h7];
  assign T97 = R93[5'h1f:5'h10];
  assign T98 = T30[1'h0:1'h0];
  assign T99 = T30[1'h1:1'h1];
  assign T100 = T30[2'h2:2'h2];
  assign T101 = T30[2'h3:2'h3];
  assign T102 = T179 ? T141 : T103;
  assign T103 = T140 ? T122 : T104;
  assign T104 = T121 ? T113 : T105;
  assign T105 = T112 ? T111 : T106;
  assign T106 = R107[4'hf:1'h0];
  assign T108 = T109 ? io_in_bits : R107;
  assign T109 = T27 & T110;
  assign T110 = T22[4'h8:4'h8];
  assign T111 = R107[5'h1f:5'h10];
  assign T112 = T30[1'h0:1'h0];
  assign T113 = T120 ? T119 : T114;
  assign T114 = R115[4'hf:1'h0];
  assign T116 = T117 ? io_in_bits : R115;
  assign T117 = T27 & T118;
  assign T118 = T22[4'h9:4'h9];
  assign T119 = R115[5'h1f:5'h10];
  assign T120 = T30[1'h0:1'h0];
  assign T121 = T30[1'h1:1'h1];
  assign T122 = T139 ? T131 : T123;
  assign T123 = T130 ? T129 : T124;
  assign T124 = R125[4'hf:1'h0];
  assign T126 = T127 ? io_in_bits : R125;
  assign T127 = T27 & T128;
  assign T128 = T22[4'ha:4'ha];
  assign T129 = R125[5'h1f:5'h10];
  assign T130 = T30[1'h0:1'h0];
  assign T131 = T138 ? T137 : T132;
  assign T132 = R133[4'hf:1'h0];
  assign T134 = T135 ? io_in_bits : R133;
  assign T135 = T27 & T136;
  assign T136 = T22[4'hb:4'hb];
  assign T137 = R133[5'h1f:5'h10];
  assign T138 = T30[1'h0:1'h0];
  assign T139 = T30[1'h1:1'h1];
  assign T140 = T30[2'h2:2'h2];
  assign T141 = T178 ? T160 : T142;
  assign T142 = T159 ? T151 : T143;
  assign T143 = T150 ? T149 : T144;
  assign T144 = R145[4'hf:1'h0];
  assign T146 = T147 ? io_in_bits : R145;
  assign T147 = T27 & T148;
  assign T148 = T22[4'hc:4'hc];
  assign T149 = R145[5'h1f:5'h10];
  assign T150 = T30[1'h0:1'h0];
  assign T151 = T158 ? T157 : T152;
  assign T152 = R153[4'hf:1'h0];
  assign T154 = T155 ? io_in_bits : R153;
  assign T155 = T27 & T156;
  assign T156 = T22[4'hd:4'hd];
  assign T157 = R153[5'h1f:5'h10];
  assign T158 = T30[1'h0:1'h0];
  assign T159 = T30[1'h1:1'h1];
  assign T160 = T177 ? T169 : T161;
  assign T161 = T168 ? T167 : T162;
  assign T162 = R163[4'hf:1'h0];
  assign T164 = T165 ? io_in_bits : R163;
  assign T165 = T27 & T166;
  assign T166 = T22[4'he:4'he];
  assign T167 = R163[5'h1f:5'h10];
  assign T168 = T30[1'h0:1'h0];
  assign T169 = T176 ? T175 : T170;
  assign T170 = R171[4'hf:1'h0];
  assign T172 = T173 ? io_in_bits : R171;
  assign T173 = T27 & T174;
  assign T174 = T22[4'hf:4'hf];
  assign T175 = R171[5'h1f:5'h10];
  assign T176 = T30[1'h0:1'h0];
  assign T177 = T30[1'h1:1'h1];
  assign T178 = T30[2'h2:2'h2];
  assign T179 = T30[2'h3:2'h3];
  assign T180 = T30[3'h4:3'h4];
  assign io_out_valid = T181;
  assign T181 = 6'h0 < R0;
  assign io_in_ready = T182;
  assign T182 = R0 < 6'h20;

  always @(posedge clk) begin
    if(reset) begin
      R0 <= 6'h0;
    end else if(T9) begin
      R0 <= T8;
    end else if(T7) begin
      R0 <= T6;
    end else if(T5) begin
      R0 <= T4;
    end
    if(T20) begin
      R18 <= io_in_bits;
    end
    if(reset) begin
      R24 <= 4'h0;
    end else if(T27) begin
      R24 <= T26;
    end
    if(reset) begin
      R31 <= 5'h0;
    end else if(T34) begin
      R31 <= T33;
    end
    if(T39) begin
      R37 <= io_in_bits;
    end
    if(T49) begin
      R47 <= io_in_bits;
    end
    if(T57) begin
      R55 <= io_in_bits;
    end
    if(T69) begin
      R67 <= io_in_bits;
    end
    if(T77) begin
      R75 <= io_in_bits;
    end
    if(T87) begin
      R85 <= io_in_bits;
    end
    if(T95) begin
      R93 <= io_in_bits;
    end
    if(T109) begin
      R107 <= io_in_bits;
    end
    if(T117) begin
      R115 <= io_in_bits;
    end
    if(T127) begin
      R125 <= io_in_bits;
    end
    if(T135) begin
      R133 <= io_in_bits;
    end
    if(T147) begin
      R145 <= io_in_bits;
    end
    if(T155) begin
      R153 <= io_in_bits;
    end
    if(T165) begin
      R163 <= io_in_bits;
    end
    if(T173) begin
      R171 <= io_in_bits;
    end
  end
endmodule

module NastiIOHostIOConverter(input clk, input reset,
    output io_nasti_aw_ready,
    input  io_nasti_aw_valid,
    input [31:0] io_nasti_aw_bits_addr,
    input [7:0] io_nasti_aw_bits_len,
    input [2:0] io_nasti_aw_bits_size,
    input [1:0] io_nasti_aw_bits_burst,
    input  io_nasti_aw_bits_lock,
    input [3:0] io_nasti_aw_bits_cache,
    input [2:0] io_nasti_aw_bits_prot,
    input [3:0] io_nasti_aw_bits_qos,
    input [3:0] io_nasti_aw_bits_region,
    input [11:0] io_nasti_aw_bits_id,
    input  io_nasti_aw_bits_user,
    output io_nasti_w_ready,
    input  io_nasti_w_valid,
    input [31:0] io_nasti_w_bits_data,
    input  io_nasti_w_bits_last,
    input [3:0] io_nasti_w_bits_strb,
    input  io_nasti_w_bits_user,
    input  io_nasti_b_ready,
    output io_nasti_b_valid,
    output[1:0] io_nasti_b_bits_resp,
    output[11:0] io_nasti_b_bits_id,
    output io_nasti_b_bits_user,
    output io_nasti_ar_ready,
    input  io_nasti_ar_valid,
    input [31:0] io_nasti_ar_bits_addr,
    input [7:0] io_nasti_ar_bits_len,
    input [2:0] io_nasti_ar_bits_size,
    input [1:0] io_nasti_ar_bits_burst,
    input  io_nasti_ar_bits_lock,
    input [3:0] io_nasti_ar_bits_cache,
    input [2:0] io_nasti_ar_bits_prot,
    input [3:0] io_nasti_ar_bits_qos,
    input [3:0] io_nasti_ar_bits_region,
    input [11:0] io_nasti_ar_bits_id,
    input  io_nasti_ar_bits_user,
    input  io_nasti_r_ready,
    output io_nasti_r_valid,
    output[1:0] io_nasti_r_bits_resp,
    output[31:0] io_nasti_r_bits_data,
    output io_nasti_r_bits_last,
    output[11:0] io_nasti_r_bits_id,
    output io_nasti_r_bits_user,
    input  io_host_clk,
    input  io_host_clk_edge,
    input  io_host_in_ready,
    output io_host_in_valid,
    output[15:0] io_host_in_bits,
    output io_host_out_ready,
    input  io_host_out_valid,
    input [15:0] io_host_out_bits,
    input  io_host_debug_stats_csr,
    output io_reset
);

  reg  T0;
  wire T1;
  wire T2;
  wire T3;
  wire T4;
  wire T5;
  wire T6;
  reg  T7;
  wire T8;
  wire T9;
  wire T10;
  wire T11;
  wire T12;
  wire T13;
  reg  T14;
  wire T15;
  wire T16;
  wire T17;
  wire T18;
  wire T19;
  reg  fifo_wen;
  wire T76;
  wire T20;
  wire T21;
  wire T22;
  wire T23;
  wire[4:0] waddr;
  wire T24;
  wire T25;
  wire T26;
  wire T27;
  reg  fifo_ren;
  wire T77;
  wire T28;
  wire T29;
  wire T30;
  wire T31;
  wire[4:0] raddr;
  wire T32;
  wire T33;
  wire T34;
  wire T35;
  reg  wr_reset;
  wire T78;
  wire T36;
  wire T37;
  wire T38;
  wire T39;
  wire T40;
  wire T41;
  wire T42;
  wire[11:0] T43;
  reg [11:0] fifo_rd_id;
  wire[11:0] T44;
  wire T45;
  wire T46;
  reg [7:0] fifo_rd_len;
  wire[7:0] T47;
  wire[7:0] T48;
  wire[7:0] T49;
  wire T50;
  wire T51;
  wire[31:0] T52;
  wire[31:0] T53;
  wire[31:0] T79;
  wire[1:0] T54;
  wire T55;
  reg  rd_count;
  wire T80;
  wire T56;
  wire T57;
  wire T58;
  wire T59;
  wire T60;
  wire T61;
  wire T62;
  wire T63;
  wire T64;
  wire[11:0] T65;
  reg [11:0] fifo_wr_id;
  wire[11:0] T66;
  wire[1:0] T67;
  reg  fifo_wr_ack;
  wire T81;
  wire T68;
  wire T69;
  wire T70;
  wire T71;
  wire T72;
  wire T73;
  wire T74;
  wire T75;
  wire hn_fifo_io_in_ready;
  wire hn_fifo_io_out_valid;
  wire[31:0] hn_fifo_io_out_bits;
  wire[5:0] hn_fifo_io_count;
  wire nh_fifo_io_in_ready;
  wire nh_fifo_io_out_valid;
  wire[15:0] nh_fifo_io_out_bits;

`ifndef SYNTHESIS
// synthesis translate_off
  integer initvar;
  initial begin
    #0.002;
    T0 = 1'b0;
    T7 = 1'b0;
    T14 = 1'b0;
    fifo_wen = {1{$random}};
    fifo_ren = {1{$random}};
    wr_reset = {1{$random}};
    fifo_rd_id = {1{$random}};
    fifo_rd_len = {1{$random}};
    rd_count = {1{$random}};
    fifo_wr_id = {1{$random}};
    fifo_wr_ack = {1{$random}};
  end
// synthesis translate_on
`endif

  assign T1 = T2 | reset;
  assign T2 = T4 | T3;
  assign T3 = io_nasti_aw_bits_burst == 2'h0;
  assign T4 = T6 | T5;
  assign T5 = io_nasti_aw_bits_len == 8'h0;
  assign T6 = io_nasti_aw_valid ^ 1'h1;
  assign T8 = T9 | reset;
  assign T9 = T11 | T10;
  assign T10 = io_nasti_ar_bits_burst == 2'h0;
  assign T11 = T13 | T12;
  assign T12 = io_nasti_ar_bits_len == 8'h0;
  assign T13 = io_nasti_ar_valid ^ 1'h1;
  assign T15 = T16 | reset;
  assign T16 = T18 | T17;
  assign T17 = io_nasti_w_bits_strb == 4'hf;
  assign T18 = io_nasti_w_valid ^ 1'h1;
  assign T19 = fifo_wen & io_nasti_w_valid;
  assign T76 = reset ? 1'h0 : T20;
  assign T20 = T25 ? 1'h0 : T21;
  assign T21 = T22 ? 1'h1 : fifo_wen;
  assign T22 = T24 & T23;
  assign T23 = waddr == 5'h0;
  assign waddr = io_nasti_aw_bits_addr[3'h6:2'h2];
  assign T24 = io_nasti_aw_ready & io_nasti_aw_valid;
  assign T25 = T26 & io_nasti_w_bits_last;
  assign T26 = io_nasti_w_ready & io_nasti_w_valid;
  assign T27 = fifo_ren & io_nasti_r_ready;
  assign T77 = reset ? 1'h0 : T28;
  assign T28 = T33 ? 1'h0 : T29;
  assign T29 = T30 ? 1'h1 : fifo_ren;
  assign T30 = T32 & T31;
  assign T31 = raddr == 5'h1;
  assign raddr = io_nasti_ar_bits_addr[3'h6:2'h2];
  assign T32 = io_nasti_ar_ready & io_nasti_ar_valid;
  assign T33 = T34 & io_nasti_r_bits_last;
  assign T34 = io_nasti_r_ready & io_nasti_r_valid;
  assign io_reset = T35;
  assign T35 = io_nasti_w_valid & wr_reset;
  assign T78 = reset ? 1'h0 : T36;
  assign T36 = T25 ? 1'h0 : T37;
  assign T37 = T38 ? 1'h1 : wr_reset;
  assign T38 = T24 & T39;
  assign T39 = T41 & T40;
  assign T40 = waddr == 5'h1f;
  assign T41 = T23 ^ 1'h1;
  assign io_host_out_ready = hn_fifo_io_in_ready;
  assign io_host_in_bits = nh_fifo_io_out_bits;
  assign io_host_in_valid = nh_fifo_io_out_valid;
  assign io_nasti_r_bits_user = T42;
  assign T42 = 1'h0;
  assign io_nasti_r_bits_id = T43;
  assign T43 = fifo_rd_id;
  assign T44 = T32 ? io_nasti_ar_bits_id : fifo_rd_id;
  assign io_nasti_r_bits_last = T45;
  assign T45 = T46;
  assign T46 = fifo_rd_len == 8'h0;
  assign T47 = T50 ? T49 : T48;
  assign T48 = T32 ? io_nasti_ar_bits_len : fifo_rd_len;
  assign T49 = fifo_rd_len - 8'h1;
  assign T50 = T34 & T51;
  assign T51 = io_nasti_r_bits_last ^ 1'h1;
  assign io_nasti_r_bits_data = T52;
  assign T52 = T53;
  assign T53 = fifo_ren ? hn_fifo_io_out_bits : T79;
  assign T79 = {26'h0, hn_fifo_io_count};
  assign io_nasti_r_bits_resp = T54;
  assign T54 = 2'h0;
  assign io_nasti_r_valid = T55;
  assign T55 = T62 | rd_count;
  assign T80 = reset ? 1'h0 : T56;
  assign T56 = T33 ? 1'h0 : T57;
  assign T57 = T58 ? 1'h1 : rd_count;
  assign T58 = T32 & T59;
  assign T59 = T61 & T60;
  assign T60 = raddr == 5'h0;
  assign T61 = T31 ^ 1'h1;
  assign T62 = fifo_ren & hn_fifo_io_out_valid;
  assign io_nasti_ar_ready = T63;
  assign T63 = fifo_ren ^ 1'h1;
  assign io_nasti_b_bits_user = T64;
  assign T64 = 1'h0;
  assign io_nasti_b_bits_id = T65;
  assign T65 = fifo_wr_id;
  assign T66 = T24 ? io_nasti_aw_bits_id : fifo_wr_id;
  assign io_nasti_b_bits_resp = T67;
  assign T67 = 2'h0;
  assign io_nasti_b_valid = fifo_wr_ack;
  assign T81 = reset ? 1'h0 : T68;
  assign T68 = T70 ? 1'h0 : T69;
  assign T69 = T25 ? 1'h1 : fifo_wr_ack;
  assign T70 = io_nasti_b_ready & io_nasti_b_valid;
  assign io_nasti_w_ready = T71;
  assign T71 = T72 | wr_reset;
  assign T72 = fifo_wen & nh_fifo_io_in_ready;
  assign io_nasti_aw_ready = T73;
  assign T73 = T75 & T74;
  assign T74 = fifo_wr_ack ^ 1'h1;
  assign T75 = fifo_wen ^ 1'h1;
  MultiWidthFifo_0 hn_fifo(.clk(clk), .reset(reset),
       .io_in_ready( hn_fifo_io_in_ready ),
       .io_in_valid( io_host_out_valid ),
       .io_in_bits( io_host_out_bits ),
       .io_out_ready( T27 ),
       .io_out_valid( hn_fifo_io_out_valid ),
       .io_out_bits( hn_fifo_io_out_bits ),
       .io_count( hn_fifo_io_count )
  );
  MultiWidthFifo_1 nh_fifo(.clk(clk), .reset(reset),
       .io_in_ready( nh_fifo_io_in_ready ),
       .io_in_valid( T19 ),
       .io_in_bits( io_nasti_w_bits_data ),
       .io_out_ready( io_host_in_ready ),
       .io_out_valid( nh_fifo_io_out_valid ),
       .io_out_bits( nh_fifo_io_out_bits )
       //.io_count(  )
  );

  always @(posedge clk) begin
`ifndef SYNTHESIS
// synthesis translate_off
  if(reset) T14 <= 1'b1;
  if(!T15 && T14 && !reset) begin
    $fwrite(32'h80000002, "ASSERTION FAILED: %s\n", "Nasti to HostIO converter cannot take partial writes");
    $finish;
  end
// synthesis translate_on
`endif
`ifndef SYNTHESIS
// synthesis translate_off
  if(reset) T7 <= 1'b1;
  if(!T8 && T7 && !reset) begin
    $fwrite(32'h80000002, "ASSERTION FAILED: %s\n", "Nasti to HostIO converter can only take fixed bursts");
    $finish;
  end
// synthesis translate_on
`endif
`ifndef SYNTHESIS
// synthesis translate_off
  if(reset) T0 <= 1'b1;
  if(!T1 && T0 && !reset) begin
    $fwrite(32'h80000002, "ASSERTION FAILED: %s\n", "Nasti to HostIO converter can only take fixed bursts");
    $finish;
  end
// synthesis translate_on
`endif
    if(reset) begin
      fifo_wen <= 1'h0;
    end else if(T25) begin
      fifo_wen <= 1'h0;
    end else if(T22) begin
      fifo_wen <= 1'h1;
    end
    if(reset) begin
      fifo_ren <= 1'h0;
    end else if(T33) begin
      fifo_ren <= 1'h0;
    end else if(T30) begin
      fifo_ren <= 1'h1;
    end
    if(reset) begin
      wr_reset <= 1'h0;
    end else if(T25) begin
      wr_reset <= 1'h0;
    end else if(T38) begin
      wr_reset <= 1'h1;
    end
    if(T32) begin
      fifo_rd_id <= io_nasti_ar_bits_id;
    end
    if(T50) begin
      fifo_rd_len <= T49;
    end else if(T32) begin
      fifo_rd_len <= io_nasti_ar_bits_len;
    end
    if(reset) begin
      rd_count <= 1'h0;
    end else if(T33) begin
      rd_count <= 1'h0;
    end else if(T58) begin
      rd_count <= 1'h1;
    end
    if(T24) begin
      fifo_wr_id <= io_nasti_aw_bits_id;
    end
    if(reset) begin
      fifo_wr_ack <= 1'h0;
    end else if(T70) begin
      fifo_wr_ack <= 1'h0;
    end else if(T25) begin
      fifo_wr_ack <= 1'h1;
    end
  end
endmodule

module ZynqAdapter(input clk, input reset,
    output io_nasti_aw_ready,
    input  io_nasti_aw_valid,
    input [31:0] io_nasti_aw_bits_addr,
    input [7:0] io_nasti_aw_bits_len,
    input [2:0] io_nasti_aw_bits_size,
    input [1:0] io_nasti_aw_bits_burst,
    input  io_nasti_aw_bits_lock,
    input [3:0] io_nasti_aw_bits_cache,
    input [2:0] io_nasti_aw_bits_prot,
    input [3:0] io_nasti_aw_bits_qos,
    input [3:0] io_nasti_aw_bits_region,
    input [11:0] io_nasti_aw_bits_id,
    input  io_nasti_aw_bits_user,
    output io_nasti_w_ready,
    input  io_nasti_w_valid,
    input [31:0] io_nasti_w_bits_data,
    input  io_nasti_w_bits_last,
    input [3:0] io_nasti_w_bits_strb,
    input  io_nasti_w_bits_user,
    input  io_nasti_b_ready,
    output io_nasti_b_valid,
    output[1:0] io_nasti_b_bits_resp,
    output[11:0] io_nasti_b_bits_id,
    output io_nasti_b_bits_user,
    output io_nasti_ar_ready,
    input  io_nasti_ar_valid,
    input [31:0] io_nasti_ar_bits_addr,
    input [7:0] io_nasti_ar_bits_len,
    input [2:0] io_nasti_ar_bits_size,
    input [1:0] io_nasti_ar_bits_burst,
    input  io_nasti_ar_bits_lock,
    input [3:0] io_nasti_ar_bits_cache,
    input [2:0] io_nasti_ar_bits_prot,
    input [3:0] io_nasti_ar_bits_qos,
    input [3:0] io_nasti_ar_bits_region,
    input [11:0] io_nasti_ar_bits_id,
    input  io_nasti_ar_bits_user,
    input  io_nasti_r_ready,
    output io_nasti_r_valid,
    output[1:0] io_nasti_r_bits_resp,
    output[31:0] io_nasti_r_bits_data,
    output io_nasti_r_bits_last,
    output[11:0] io_nasti_r_bits_id,
    output io_nasti_r_bits_user,
    input  io_host_clk,
    input  io_host_clk_edge,
    input  io_host_in_ready,
    output io_host_in_valid,
    output[15:0] io_host_in_bits,
    output io_host_out_ready,
    input  io_host_out_valid,
    input [15:0] io_host_out_bits,
    input  io_host_debug_stats_csr,
    output io_reset
);

  wire conv_io_nasti_aw_ready;
  wire conv_io_nasti_w_ready;
  wire conv_io_nasti_b_valid;
  wire[1:0] conv_io_nasti_b_bits_resp;
  wire[11:0] conv_io_nasti_b_bits_id;
  wire conv_io_nasti_b_bits_user;
  wire conv_io_nasti_ar_ready;
  wire conv_io_nasti_r_valid;
  wire[1:0] conv_io_nasti_r_bits_resp;
  wire[31:0] conv_io_nasti_r_bits_data;
  wire conv_io_nasti_r_bits_last;
  wire[11:0] conv_io_nasti_r_bits_id;
  wire conv_io_nasti_r_bits_user;
  wire conv_io_host_in_valid;
  wire[15:0] conv_io_host_in_bits;
  wire conv_io_host_out_ready;
  wire conv_io_reset;


  assign io_reset = conv_io_reset;
  assign io_host_out_ready = conv_io_host_out_ready;
  assign io_host_in_bits = conv_io_host_in_bits;
  assign io_host_in_valid = conv_io_host_in_valid;
  assign io_nasti_r_bits_user = conv_io_nasti_r_bits_user;
  assign io_nasti_r_bits_id = conv_io_nasti_r_bits_id;
  assign io_nasti_r_bits_last = conv_io_nasti_r_bits_last;
  assign io_nasti_r_bits_data = conv_io_nasti_r_bits_data;
  assign io_nasti_r_bits_resp = conv_io_nasti_r_bits_resp;
  assign io_nasti_r_valid = conv_io_nasti_r_valid;
  assign io_nasti_ar_ready = conv_io_nasti_ar_ready;
  assign io_nasti_b_bits_user = conv_io_nasti_b_bits_user;
  assign io_nasti_b_bits_id = conv_io_nasti_b_bits_id;
  assign io_nasti_b_bits_resp = conv_io_nasti_b_bits_resp;
  assign io_nasti_b_valid = conv_io_nasti_b_valid;
  assign io_nasti_w_ready = conv_io_nasti_w_ready;
  assign io_nasti_aw_ready = conv_io_nasti_aw_ready;
  NastiIOHostIOConverter conv(.clk(clk), .reset(reset),
       .io_nasti_aw_ready( conv_io_nasti_aw_ready ),
       .io_nasti_aw_valid( io_nasti_aw_valid ),
       .io_nasti_aw_bits_addr( io_nasti_aw_bits_addr ),
       .io_nasti_aw_bits_len( io_nasti_aw_bits_len ),
       .io_nasti_aw_bits_size( io_nasti_aw_bits_size ),
       .io_nasti_aw_bits_burst( io_nasti_aw_bits_burst ),
       .io_nasti_aw_bits_lock( io_nasti_aw_bits_lock ),
       .io_nasti_aw_bits_cache( io_nasti_aw_bits_cache ),
       .io_nasti_aw_bits_prot( io_nasti_aw_bits_prot ),
       .io_nasti_aw_bits_qos( io_nasti_aw_bits_qos ),
       .io_nasti_aw_bits_region( io_nasti_aw_bits_region ),
       .io_nasti_aw_bits_id( io_nasti_aw_bits_id ),
       .io_nasti_aw_bits_user( io_nasti_aw_bits_user ),
       .io_nasti_w_ready( conv_io_nasti_w_ready ),
       .io_nasti_w_valid( io_nasti_w_valid ),
       .io_nasti_w_bits_data( io_nasti_w_bits_data ),
       .io_nasti_w_bits_last( io_nasti_w_bits_last ),
       .io_nasti_w_bits_strb( io_nasti_w_bits_strb ),
       .io_nasti_w_bits_user( io_nasti_w_bits_user ),
       .io_nasti_b_ready( io_nasti_b_ready ),
       .io_nasti_b_valid( conv_io_nasti_b_valid ),
       .io_nasti_b_bits_resp( conv_io_nasti_b_bits_resp ),
       .io_nasti_b_bits_id( conv_io_nasti_b_bits_id ),
       .io_nasti_b_bits_user( conv_io_nasti_b_bits_user ),
       .io_nasti_ar_ready( conv_io_nasti_ar_ready ),
       .io_nasti_ar_valid( io_nasti_ar_valid ),
       .io_nasti_ar_bits_addr( io_nasti_ar_bits_addr ),
       .io_nasti_ar_bits_len( io_nasti_ar_bits_len ),
       .io_nasti_ar_bits_size( io_nasti_ar_bits_size ),
       .io_nasti_ar_bits_burst( io_nasti_ar_bits_burst ),
       .io_nasti_ar_bits_lock( io_nasti_ar_bits_lock ),
       .io_nasti_ar_bits_cache( io_nasti_ar_bits_cache ),
       .io_nasti_ar_bits_prot( io_nasti_ar_bits_prot ),
       .io_nasti_ar_bits_qos( io_nasti_ar_bits_qos ),
       .io_nasti_ar_bits_region( io_nasti_ar_bits_region ),
       .io_nasti_ar_bits_id( io_nasti_ar_bits_id ),
       .io_nasti_ar_bits_user( io_nasti_ar_bits_user ),
       .io_nasti_r_ready( io_nasti_r_ready ),
       .io_nasti_r_valid( conv_io_nasti_r_valid ),
       .io_nasti_r_bits_resp( conv_io_nasti_r_bits_resp ),
       .io_nasti_r_bits_data( conv_io_nasti_r_bits_data ),
       .io_nasti_r_bits_last( conv_io_nasti_r_bits_last ),
       .io_nasti_r_bits_id( conv_io_nasti_r_bits_id ),
       .io_nasti_r_bits_user( conv_io_nasti_r_bits_user ),
       .io_host_clk( io_host_clk ),
       .io_host_clk_edge( io_host_clk_edge ),
       .io_host_in_ready( io_host_in_ready ),
       .io_host_in_valid( conv_io_host_in_valid ),
       .io_host_in_bits( conv_io_host_in_bits ),
       .io_host_out_ready( conv_io_host_out_ready ),
       .io_host_out_valid( io_host_out_valid ),
       .io_host_out_bits( io_host_out_bits ),
       .io_host_debug_stats_csr( io_host_debug_stats_csr ),
       .io_reset( conv_io_reset )
  );
endmodule

